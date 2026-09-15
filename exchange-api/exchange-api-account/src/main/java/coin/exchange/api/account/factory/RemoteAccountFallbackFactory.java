package coin.exchange.api.account.factory;

import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.api.account.dto.CreateAccountWalletDto;
import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoteAccountFallbackFactory implements FallbackFactory<RemoteAccountService> {
    @Override
    public RemoteAccountService create(Throwable throwable) {
        log.error("【Feign异常】账户服务调用失败:{}", throwable.getMessage());
        return new RemoteAccountService() {
            @Override
            public R<Long> createWallet(CreateAccountWalletDto walletDto) {
                return R.fail("【Feign异常】创建账户钱包失败" + throwable.getMessage());
            }

            @Override
            public R<AccountWalletVo> getWalletBalance(Long userId) {
                return R.fail("【Feign异常】调取账户钱包信息失败" + throwable.getMessage());
            }

            @Override
            public R<String> frozenAssets(AccountFrozenAssetsDto accountFrozenAssetsDto) {
                return R.fail("【Feign异常】冻结资产操作失败" + throwable.getMessage());
            }

            @Override
            public R<String> deductAssets(AccountFrozenAssetsDto assetsDto) {
                return R.fail("【Feign异常】扣除资产操作失败" + throwable.getMessage());
            }
        };
    }
}
