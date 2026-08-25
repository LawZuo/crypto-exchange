package coin.exchange.auth.service;

import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserAuthVo;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.auth.dto.LoginDto;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.IpUtil;
import cn.hutool.core.bean.BeanUtil;
import coin.exchange.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginService {

    private final RemoteUserService remoteUserService;

    /**
     * 登录
     */
    public UserVo login(LoginDto loginDto, HttpServletRequest request) {

        log.info("【用户登录】账号：{}", loginDto.getUsername());

        // 获取用户的IP地址
        String ip = IpUtil.getClientIp(
                request.getHeader("X-Real-Client-IP"),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr()
        );
        log.info("【用户登录】IP：{}", ip);

        R<UserAuthVo> result = remoteUserService.getUserAuthInfo(SecurityConstants.INNER, loginDto.getUsername());
        log.debug("【用户登录】Feign获取认证信息成功: {}", result != null && result.code() == R.SUCCESS_CODE);
        UserAuthVo user = result == null ? null : result.getData();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户被禁用");
        }
        if (!SecurityUtils.matchesPassword(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        recordLogin(user.getId(), ip, request);
        return userVo;
    }

    private void recordLogin(Long userId, String ip, HttpServletRequest request) {
        LoginRecordDto dto = new LoginRecordDto();
        dto.setUserId(userId);
        dto.setLoginIp(ip);
        dto.setDeviceSource(resolveDeviceSource(request));
        dto.setDeviceInfo(resolveDeviceInfo(request));
        R<Void> result = remoteUserService.recordLogin(SecurityConstants.INNER, dto);
        if (result == null || result.code() != R.SUCCESS_CODE) {
            String message = result == null ? "调用用户服务无响应" : result.message();
            log.warn("【用户登录】记录登录信息失败：userId={}, ip={}, message={}", userId, ip, message);
        }
    }

    private String resolveDeviceSource(HttpServletRequest request) {
        String source = request.getHeader("X-Device-Source");
        if (source == null || source.isBlank()) {
            source = request.getHeader("X-Client-Type");
        }
        return source;
    }

    private String resolveDeviceInfo(HttpServletRequest request) {
        String deviceInfo = request.getHeader("X-Device-Info");
        if (deviceInfo == null || deviceInfo.isBlank()) {
            deviceInfo = request.getHeader("User-Agent");
        }
        return deviceInfo;
    }

    /**
     * 注册
     */
    public R<Long> register(RegisterUserDto dto) {
        log.info("【用户注册】账号：{}，邮箱：{}", dto.getUsername(), dto.getEmail());
        return remoteUserService.registerUser(dto);
    }
}
