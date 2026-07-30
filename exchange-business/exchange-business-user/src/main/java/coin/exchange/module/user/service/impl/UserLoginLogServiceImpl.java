package coin.exchange.module.user.service.impl;

import coin.exchange.module.user.domain.UserLoginLogDo;
import coin.exchange.module.user.mapper.UserLoginLogMapper;
import coin.exchange.module.user.service.UserLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserLoginLogServiceImpl implements UserLoginLogService {

    private static final int DEVICE_SOURCE_MAX_LENGTH = 10;
    private static final int DEVICE_INFO_MAX_LENGTH = 10;
    private static final int FAIL_REASON_MAX_LENGTH = 225;

    private final UserLoginLogMapper userLoginLogMapper;

    @Override
    public Long createLoginLog(UserLoginLogDo loginLog) {
        if (loginLog == null) {
            throw new IllegalArgumentException("登录日志不能为空");
        }
        if (loginLog.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (loginLog.getStatus() == null) {
            loginLog.setStatus(STATUS_SUCCESS);
        }
        loginLog.setDeviceSource(limit(loginLog.getDeviceSource(), DEVICE_SOURCE_MAX_LENGTH));
        loginLog.setDeviceInfo(limit(loginLog.getDeviceInfo(), DEVICE_INFO_MAX_LENGTH));
        loginLog.setFailReason(limit(loginLog.getFailReason(), FAIL_REASON_MAX_LENGTH));

        userLoginLogMapper.insert(loginLog);
        return loginLog.getId();
    }

    @Override
    public Long recordSuccess(Long userId, String ipAddress, String deviceSource, String deviceInfo) {
        UserLoginLogDo loginLog = new UserLoginLogDo();
        loginLog.setUserId(userId);
        loginLog.setIpAddress(ipAddress);
        loginLog.setDeviceSource(deviceSource);
        loginLog.setDeviceInfo(deviceInfo);
        loginLog.setStatus(STATUS_SUCCESS);
        return createLoginLog(loginLog);
    }

    @Override
    public Long recordFailure(Long userId, String ipAddress, String deviceSource, String deviceInfo, String failReason) {
        UserLoginLogDo loginLog = new UserLoginLogDo();
        loginLog.setUserId(userId);
        loginLog.setIpAddress(ipAddress);
        loginLog.setDeviceSource(deviceSource);
        loginLog.setDeviceInfo(deviceInfo);
        loginLog.setStatus(STATUS_FAIL);
        loginLog.setFailReason(failReason);
        return createLoginLog(loginLog);
    }

    private String limit(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
