package coin.exchange.business.account.service;

import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.business.account.domain.AccountWalletDo;
import coin.exchange.common.core.enums.WalletTypeCode;

import java.util.List;

/**
 * 钱包服务
 */
public interface AccountWalletService {

    /**
     * 创建钱包
     */
    Long createWallet(AccountWalletDo wallet);

    /**
     * 根据ID查询钱包
     */
    AccountWalletDo getWallet(Long id);

    /**
     * 根据用户、币种和钱包类型查询钱包
     */
    AccountWalletDo getWallet(Long userId, String currency, Integer walletType);

    /**
     * 根据用户ID查询钱包列表
     */
    List<AccountWalletDo> listWallets(Long userId);

    /**
     * 冻结钱包余额
     */
    void frozenBalance(AccountFrozenAssetsDto assetsDto);

    /**
     * 扣除钱包余额
     */
    void deductBalance(AccountFrozenAssetsDto assetsDto);
}
