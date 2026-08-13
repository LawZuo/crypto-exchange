package coin.exchange.api.account.service;

import coin.exchange.api.account.factory.RemoteAccountFallbackFactory;
import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "exchange-business-account",
        url = "${bt.upstream.base-url:http://localhost:8082}",
        fallbackFactory = RemoteAccountFallbackFactory.class
)
public interface RemoteAccountService {

    /**
     * 通过用户ID获取钱包信息
     */
    @GetMapping("/account/wallet/balance/{userId}")
    R<AccountWalletVo> getWalletBalance(@PathVariable("userId") Long userId);
}
