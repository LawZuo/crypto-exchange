package coin.exchange.business.account.service;

import coin.exchange.business.account.domain.AccountWalletDo;

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
     * 更新钱包
     */
    int updateWallet(AccountWalletDo wallet);

    /**
     * 删除钱包
     */
    int deleteWallet(Long id);
}
