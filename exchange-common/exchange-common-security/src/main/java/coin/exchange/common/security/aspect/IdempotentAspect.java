package coin.exchange.common.security.aspect;

import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.context.SecurityContextHolder;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.ServletUtils;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.common.security.annotation.Idempotent;
import coin.exchange.common.security.exception.DuplicateSubmitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * 基于 Redis 原子占位实现接口级防重复提交。
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String LOCK_VALUE = "1";

    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(coin.exchange.common.security.annotation.Idempotent) || "
            + "@within(coin.exchange.common.security.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Idempotent idempotent = resolveAnnotation(joinPoint);
        if (idempotent == null) {
            return joinPoint.proceed();
        }

        String redisKey = buildRedisKey(joinPoint, idempotent);
        boolean acquired = redisService.setIfAbsent(redisKey, LOCK_VALUE, idempotent.expire(), idempotent.timeUnit());
        if (!acquired) {
            log.warn("拦截重复提交: key={}", redisKey);
            return duplicateResponse(joinPoint, idempotent);
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (idempotent.releaseOnFailure()) {
                redisService.deleteObject(redisKey);
            }
            throw throwable;
        }
    }

    private Idempotent resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent annotation = AnnotationUtils.findAnnotation(method, Idempotent.class);
        if (annotation != null) {
            return annotation;
        }
        Class<?> targetClass = joinPoint.getTarget() == null ? method.getDeclaringClass() : joinPoint.getTarget().getClass();
        return AnnotationUtils.findAnnotation(targetClass, Idempotent.class);
    }

    private Object duplicateResponse(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        if (R.class.isAssignableFrom(returnType)) {
            return R.fail(StatusCode.BAD_REQUEST, idempotent.message());
        }
        throw new DuplicateSubmitException(idempotent.message());
    }

    private String buildRedisKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        HttpServletRequest request = ServletUtils.getRequest();
        String identity = resolveIdentity(request);
        String businessKey = resolveBusinessKey(joinPoint, idempotent, request);
        String rawKey = idempotent.prefix() + ":" + identity + ":" + businessKey;
        return idempotent.prefix() + ":" + sha256(rawKey);
    }

    private String resolveBusinessKey(ProceedingJoinPoint joinPoint, Idempotent idempotent, HttpServletRequest request) {
        if (StringUtils.hasText(idempotent.key())) {
            Object value = parseExpression(joinPoint, request, idempotent.key());
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }

        String headerValue = request == null ? null : request.getHeader(idempotent.header());
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }

        return requestFingerprint(joinPoint, request);
    }

    private Object parseExpression(ProceedingJoinPoint joinPoint, HttpServletRequest request, String expression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("request", request);
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (parameterNames != null && i < parameterNames.length) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return expressionParser.parseExpression(expression).getValue(context);
    }

    private String resolveIdentity(HttpServletRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        if (request != null) {
            String token = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
            if (StringUtils.hasText(token)) {
                return "token:" + token;
            }
            String ip = request.getRemoteAddr();
            if (StringUtils.hasText(ip)) {
                return "ip:" + ip;
            }
        }
        return "anonymous";
    }

    private String requestFingerprint(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        List<Object> parts = new ArrayList<>();
        if (request != null) {
            parts.add(request.getMethod());
            parts.add(request.getRequestURI());
            parts.add(request.getQueryString());
        }
        parts.add(filteredArgs(joinPoint.getArgs()));
        return sha256(toJson(parts));
    }

    private List<Object> filteredArgs(Object[] args) {
        List<Object> values = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse) {
                continue;
            }
            values.add(arg);
        }
        return values;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
