package coin.exchange.web.controller;

import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class WebUserController {

    private final RemoteUserService remoteUserService;
    private final RemoteAccountService remoteAccountService;

    @GetMapping("/{username}")
    public R<UserVo> getUser(@PathVariable("username") String username) {
        return remoteUserService.getUserInfo(username);
    }

    @GetMapping("/{userId}/wallet")
    public R<AccountWalletVo> getWallet(@PathVariable("userId") Long userId) {
        return remoteAccountService.getWalletBalance(userId);
    }
}
