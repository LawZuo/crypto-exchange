package coin.exchange.api.account.service;

import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.api.account.dto.CreateAccountWalletDto;
import coin.exchange.api.account.factory.RemoteAccountFallbackFactory;
import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "exchange-business-account",
        fallbackFactory = RemoteAccountFallbackFactory.class
)
public interface RemoteAccountService {

    /**
     * 获取或创建钱包，重复调用返回已有钱包ID。
     */
    @PostMapping("/account/wallet/create")
    R<Long> createWallet(@RequestBody CreateAccountWalletDto walletDto);

    /**
     * 通过用户ID获取钱包信息
     */
    @GetMapping("/account/wallet/balance/{userId}")
    R<AccountWalletVo> getWalletBalance(@PathVariable("userId") Long userId);

    /**
     * 冻结资产
     */
    @PostMapping("/account/wallet/frozen")
    R<String> frozenAssets(@RequestBody AccountFrozenAssetsDto accountFrozenAssetsDto);

    /**
     * 扣除资产
     */
    @PostMapping("/account/wallet/deduct")
    R<String> deductAssets(@RequestBody AccountFrozenAssetsDto assetsDto);
}
