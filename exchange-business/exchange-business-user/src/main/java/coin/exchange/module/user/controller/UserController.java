package coin.exchange.module.user.controller;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.account.dto.CreateAccountWalletDto;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserAuthVo;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.enums.WalletTypeCode;
import coin.exchange.common.core.exception.BusinessException;
import coin.exchange.common.core.response.R;
import coin.exchange.common.core.utils.ServletUtils;
import coin.exchange.common.security.annotation.Idempotent;
import coin.exchange.module.user.domain.UserDo;
import coin.exchange.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制层
 */
@RequestMapping("/user/information")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RemoteAccountService remoteAccountService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{username}")
    public R<UserVo> getUser(
            @PathVariable("username") String username,
            HttpServletRequest request
    ) {
        log.info("用户信息接口被调用，参数：{}", username);
        log.info("请求头信息：{}", ServletUtils.getHeaders(request));
        UserDo user = userService.getUserByUsername(username);
        log.warn("查询到的用户信息：{}", user);

        UserVo userVO = new UserVo();
        BeanUtil.copyProperties(user, userVO);
        log.warn("将返回的用户信息：{}", userVO);
        return R.success(userVO);
    }

    /**
     * 获取用户认证信息
     */
    @GetMapping("/auth/{username}")
    public R<UserAuthVo> getUserAuth(
            @RequestHeader(value = SecurityConstants.FROM_SOURCE, required = false) String source,
            @PathVariable("username") String username
    ) {
        if (!SecurityConstants.INNER.equals(source)) {
            throw new BusinessException(StatusCode.FORBIDDEN, "非法内部接口调用");
        }
        UserAuthVo userAuthVo = userService.getUserAuthByUsername(username);
        return R.success(userAuthVo);
    }

    /** 通过邮箱获取用户认证信息，仅供内部服务调用。 */
    @GetMapping("/auth/email/{email}")
    public R<UserAuthVo> getUserAuthByEmail(
            @RequestHeader(value = SecurityConstants.FROM_SOURCE, required = false) String source,
            @PathVariable("email") String email
    ) {
        if (!SecurityConstants.INNER.equals(source)) {
            throw new BusinessException(StatusCode.FORBIDDEN, "非法内部接口调用");
        }
        return R.success(userService.getUserAuthByEmail(email));
    }

    /**
     * 注册用户
     */
    @PostMapping("/register")
    @Idempotent(prefix = "user:register", key = "#p0.username", expire = 30, message = "注册请求正在处理，请勿重复提交")
    public R<Long> registerUser(@Valid @RequestBody RegisterUserDto dto, HttpServletRequest request) {
        try {
            log.info("注册用户信息接口被调用，账号：{}，邮箱：{}", dto.getUsername(), dto.getEmail());
            Long userId = userService.createUser(dto, request);

            // 初始化钱包
            if (userId != null) {
                CreateAccountWalletDto walletDto = new CreateAccountWalletDto();
                walletDto.setCurrency("USDT");
                walletDto.setUserId(userId);
                walletDto.setWalletType(WalletTypeCode.ASSETS);
                R<Long> walletResult = remoteAccountService.createWallet(walletDto);
                if (walletResult == null || walletResult.code() != R.SUCCESS_CODE || walletResult.getData() == null) {
                    String message = walletResult == null ? "账户服务无响应" : walletResult.message();
                    log.error("初始化USDT钱包失败，userId: {}，message: {}", userId, message);
                    throw new BusinessException(StatusCode.INTERNAL_ERROR, "初始化USDT钱包失败：" + message);
                }
                log.info("初始化USDT钱包成功，userId: {}，walletId: {}", userId, walletResult.getData());
            }

            return R.success(userId);
        } catch (RuntimeException e) {
            log.error("注册用户失败", e);
            return R.fail(e.toString());
        }
    }

    /**
     * 记录用户登录信息
     */
    @PostMapping("/login-record")
    public R<Void> recordLogin(
            @RequestHeader(value = SecurityConstants.FROM_SOURCE, required = false) String source,
            @Valid @RequestBody LoginRecordDto dto
    ) {
        log.info("记录用户登录信息接口被调用，参数：{}", dto);
        if (!SecurityConstants.INNER.equals(source)) {
            throw new BusinessException(StatusCode.FORBIDDEN, "非法内部接口调用");
        }
        userService.recordLogin(dto.getUserId(), dto.getLoginIp(), dto.getDeviceSource(), dto.getDeviceInfo());
        return R.success(null);
    }
}
