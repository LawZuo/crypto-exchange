package coin.exchange.auth.controller;

import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.LoginVo;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.auth.dto.LoginDto;
import coin.exchange.auth.service.LoginService;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.JwtUtil;
import coin.exchange.common.core.utils.ServletUtils;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.common.security.annotation.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LoginService loginService;
    private final RedisService redisService;

    @PostMapping("/login")
    public R<LoginVo> login(
            @RequestBody LoginDto loginDto,
            HttpServletRequest request
    ) {
        try {
            log.info("【用户登录】账号：{}，密码：{}", loginDto.getUsername(), loginDto.getPassword());
            log.info("【用户登录】请求头信息：{}", ServletUtils.getHeaders(request));
            UserVo userVo = loginService.login(loginDto, request);

            // 获取当前时间
            Date now = new Date();

            // 创建token
            String token = JwtUtil.generate(userVo.getId(), userVo.getUsername(), now);
            log.info("【用户登录】token:{}", token);
            LoginVo loginVo = new LoginVo();
            loginVo.setId(userVo.getId().toString());
            loginVo.setUsername(userVo.getUsername());
            loginVo.setToken(token);
            loginVo.setUser(userVo);
            loginVo.setLoginTime(String.valueOf(now.getTime()));
            loginVo.setExpireTime(String.valueOf(JwtUtil.getExpireMs(now)));

            // 缓存 token
            redisService.setCacheObject("user:login:" + token, userVo.getId(), JwtUtil.EXPIRE_MS);

            return R.success(loginVo);
        } catch (Exception e) {
            log.error("登录异常, {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public R<String> logout() {
        return R.success("logout");
    }

    @PostMapping("/register")
    @Idempotent(prefix = "auth:register", key = "#p0.username", expire = 30, message = "注册请求正在处理，请勿重复提交")
    public R<Long> register(
            @RequestBody RegisterUserDto registerUserDto
    ) {
        try {
            return loginService.register(registerUserDto);
        } catch (Exception e) {
            log.error("注册异常, {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }
}
