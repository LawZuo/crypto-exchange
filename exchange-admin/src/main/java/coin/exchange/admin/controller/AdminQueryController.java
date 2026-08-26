package coin.exchange.admin.controller;

import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.api.market.service.RemoteMarketService;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/management")
@RequiredArgsConstructor
public class AdminQueryController {

    private final RemoteUserService remoteUserService;
    private final RemoteAccountService remoteAccountService;
    private final RemoteMarketService remoteMarketService;

    @GetMapping("/users/{username}")
    public R<UserVo> getUser(@PathVariable("username") String username) {
        return remoteUserService.getUserInfo(username);
    }

    @GetMapping("/accounts/{userId}/wallet")
    public R<AccountWalletVo> getWallet(@PathVariable("userId") Long userId) {
        return remoteAccountService.getWalletBalance(userId);
    }

    @GetMapping("/market/symbols")
    public R<List<MarketSymbolVo>> listSymbols() {
        return remoteMarketService.listSymbols();
    }
}
