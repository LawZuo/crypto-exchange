package coin.exchange.auth.service;

import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.auth.dto.LoginDto;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录服务
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class LoginService {

    private final RemoteUserService remoteUserService;

    /**
     * 登录
     */
    public UserVo login(LoginDto loginDto, HttpServletRequest request) {

        log.info("【用户登录】账号：{}，密码：{}", loginDto.getUsername(), loginDto.getPassword());

        // 获取用户的IP地址
        String ip = IpUtil.getClientIp(
                request.getHeader("X-Real-Client-IP"),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr()
        );
        log.info("【用户登录】IP：{}", ip);

        R<UserVo> result = remoteUserService.getUserInfo(loginDto.getUsername());
        log.info("【用户登录】Feign获取用户信息:{}", result);
        UserVo user = result == null ? null : result.getData();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户被禁用");
        }
        recordLogin(user.getId(), ip);
        return user;
    }

    private void recordLogin(Long userId, String ip) {
        LoginRecordDto dto = new LoginRecordDto();
        dto.setUserId(userId);
        dto.setLoginIp(ip);
        R<Void> result = remoteUserService.recordLogin(SecurityConstants.INNER, dto);
        if (result == null || result.code() != R.SUCCESS_CODE) {
            String message = result == null ? "调用用户服务无响应" : result.message();
            log.warn("【用户登录】记录登录信息失败：userId={}, ip={}, message={}", userId, ip, message);
        }
    }

    /**
     * 注册
     */
    public R<Long> register(RegisterUserDto dto) {
        log.info("【用户注册】注册信息:{}", dto.toString());
        return remoteUserService.registerUser(dto);
    }
}
