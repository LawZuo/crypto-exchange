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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Tag(name = "认证服务", description = "用户登录、注册、登出接口")
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LoginService loginService;
    private final RedisService redisService;

    @Operation(summary = "用户登录")
    @ApiResponse(responseCode = "200", description = "用户登录成功")
    @ApiResponse(responseCode = "400", description = "用户登录失败")
    @PostMapping("/login")
    public R<LoginVo> login(
            @Valid @RequestBody LoginDto loginDto,
            HttpServletRequest request
    ) {
        log.info("【用户登录】账号：{}", loginDto.getUsername());
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
    }

    @Operation(summary = "用户登出")
    @ApiResponse(responseCode = "200", description = "用户登出成功")
    @ApiResponse(responseCode = "400", description = "用户登出失败")
    @PostMapping("/logout")
    public R<String> logout() {
        return R.success("logout");
    }

    @Operation(summary = "用户注册")
    @ApiResponse(responseCode = "200", description = "用户注册成功")
    @ApiResponse(responseCode = "400", description = "用户注册失败")
    @PostMapping("/register")
    @Idempotent(prefix = "auth:register", key = "#p0.username", expire = 30, message = "注册请求正在处理，请勿重复提交")
    public R<Long> register(
            @Valid @RequestBody RegisterUserDto registerUserDto
    ) {
        log.info("【用户注册】账号：{}，邮箱：{}", registerUserDto.getUsername(), registerUserDto.getEmail());
        return loginService.register(registerUserDto);
    }
}
