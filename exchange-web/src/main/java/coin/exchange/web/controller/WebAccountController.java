package coin.exchange.web.controller;

import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class WebAccountController {

    private final RemoteAccountService remoteAccountService;

    // 获取用户钱包
    @GetMapping("/{userId}/wallet")
    public R<List<AccountWalletVo>> getWallet(@PathVariable("userId") Long userId) {
        return remoteAccountService.getWalletBalance(userId);
    }
}
