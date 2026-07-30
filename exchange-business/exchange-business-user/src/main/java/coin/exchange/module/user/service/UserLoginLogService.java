package coin.exchange.module.user.service;

import coin.exchange.module.user.domain.UserLoginLogDo;

public interface UserLoginLogService {

    int STATUS_FAIL = 0;
    int STATUS_SUCCESS = 1;

    /**
     * 新增登录日志。
     */
    Long createLoginLog(UserLoginLogDo loginLog);

    /**
     * 记录登录成功日志。
     */
    Long recordSuccess(Long userId, String ipAddress, String deviceSource, String deviceInfo);

    /**
     * 记录登录失败日志。
     */
    Long recordFailure(Long userId, String ipAddress, String deviceSource, String deviceInfo, String failReason);
}
