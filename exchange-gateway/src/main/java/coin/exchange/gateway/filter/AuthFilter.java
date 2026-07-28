package coin.exchange.gateway.filter;

import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.JwtUtil;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.gateway.config.GatewaySecurityProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

/**
 * 网关鉴权过滤器。解析 token 后将用户信息透传给下游服务。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "user:login:";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final GatewaySecurityProperties securityProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (isExcluded(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "未提供认证token");
        }

        return Mono.fromCallable(() -> parseAndCheck(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(claims -> {
                    String userId = claims.getSubject();
                    String username = String.valueOf(claims.get("username"));
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .headers(headers -> {
                                headers.set(SecurityConstants.AUTHORIZATION_HEADER, token);
                                headers.set(SecurityConstants.DETAILS_USER_ID, userId);
                                headers.set(SecurityConstants.DETAILS_USERNAME, username);
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.warn("网关鉴权失败: {}", e.getMessage());
                    return unauthorized(exchange, "认证失败");
                });
    }

    private Claims parseAndCheck(String token) {
        Claims claims = JwtUtil.parse(token);
        Boolean exists = redisService.hasKey(LOGIN_TOKEN_KEY_PREFIX + token);
        if (!Boolean.TRUE.equals(exists)) {
            throw new IllegalArgumentException("token已过期");
        }
        return claims;
    }

    private String resolveToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length()).trim();
        }
        return token;
    }

    private boolean isExcluded(String path) {
        for (String excludePath : securityProperties.getIgnorePaths()) {
            if (excludePath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = toJson(R.fail(StatusCode.UNAUTHORIZED, message)).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"code\":500,\"message\":\"服务器内部错误\",\"data\":null}";
        }
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
