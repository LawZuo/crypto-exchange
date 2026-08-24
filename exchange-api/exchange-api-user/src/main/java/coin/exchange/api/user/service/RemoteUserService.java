package coin.exchange.api.user.service;

import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.factory.RemoteUserFallbackFactory;
import coin.exchange.api.user.model.UserAuthVo;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 远程用户服务 Feign 客户端。
 *
 * 注意：当前部署未启用服务注册中心（nacos discovery 已关），
 * 这里通过 url 指定绝对地址，绕过 LoadBalancer。
 * 后续若启用 Nacos，删掉 url 即可走服务名。
 */
@FeignClient(
        name = "exchange-business-user",
        url = "${bt.upstream.base-url:http://localhost:9001}",
        fallbackFactory = RemoteUserFallbackFactory.class
)
public interface RemoteUserService {
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 结果
     */
    @GetMapping("/user/information/{username}")
    public R<UserVo> getUserInfo(@PathVariable("username") String username);

    /**
     * 通过用户名查询认证信息
     */
    @GetMapping("/user/information/auth/{username}")
    public R<UserAuthVo> getUserAuthInfo(
            @RequestHeader(value = SecurityConstants.FROM_SOURCE, required = false) String source,
            @PathVariable("username") String username
    );

    /**
     * 注册用户
     */
    @PostMapping("/user/information/register")
    public R<Long> registerUser(@RequestBody RegisterUserDto dto);

    /**
     * 记录用户登录信息
     */
    @PostMapping("/user/information/login-record")
    public R<Void> recordLogin(
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestBody LoginRecordDto dto
    );
}
