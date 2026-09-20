package coin.exchange.auth.service;

import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserAuthVo;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.auth.dto.LoginDto;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.exception.BusinessException;
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

        String loginName = resolveLoginName(loginDto);
        log.info("【用户登录】登录类型：{}，登录标识：{}", loginDto.getLoginType(), loginName);

        // 获取用户的IP地址
        String ip = IpUtil.getClientIp(
                request.getHeader("X-Real-Client-IP"),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr()
        );
        log.info("【用户登录】IP：{}", ip);

        R<UserAuthVo> result = loginDto.getLoginType() == 2
                ? remoteUserService.getUserAuthInfoByEmail(SecurityConstants.INNER, loginName)
                : remoteUserService.getUserAuthInfo(SecurityConstants.INNER, loginName);
        log.debug("【用户登录】Feign获取认证信息成功: {}", result != null && result.code() == R.SUCCESS_CODE);
        if (result == null) {
            throw new BusinessException(StatusCode.INTERNAL_ERROR, "用户服务无响应");
        }
        if (result.code() != R.SUCCESS_CODE) {
            throw new BusinessException(StatusCode.INTERNAL_ERROR, result.message());
        }

        UserAuthVo user = result.getData();
        if (user == null) {
            throw new BusinessException(StatusCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(StatusCode.USER_DISABLED);
        }
        if (!SecurityUtils.matchesPassword(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException(StatusCode.USER_PASSWORD_ERROR);
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        recordLogin(user.getId(), ip, request);
        return userVo;
    }

    private String resolveLoginName(LoginDto loginDto) {
        if (loginDto.getLoginType() == 1) {
            if (loginDto.getUsername() == null || loginDto.getUsername().isBlank()) {
                throw new BusinessException(StatusCode.BAD_REQUEST, "账号不能为空");
            }
            return loginDto.getUsername().trim();
        }
        if (loginDto.getLoginType() == 2) {
            if (loginDto.getEmail() == null || loginDto.getEmail().isBlank()) {
                throw new BusinessException(StatusCode.BAD_REQUEST, "邮箱不能为空");
            }
            return loginDto.getEmail().trim();
        }
        throw new BusinessException(StatusCode.BAD_REQUEST, "登录类型只支持1-账号或2-邮箱");
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
