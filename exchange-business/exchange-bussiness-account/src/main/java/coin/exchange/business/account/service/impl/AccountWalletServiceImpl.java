package coin.exchange.business.account.service.impl;

import coin.exchange.business.account.domain.AccountWalletDo;
import coin.exchange.business.account.mapper.AccountWalletMapper;
import coin.exchange.business.account.service.AccountWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AccountWalletServiceImpl implements AccountWalletService {

    private final AccountWalletMapper accountWalletMapper;

    @Override
    public Long createWallet(AccountWalletDo wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("钱包信息不能为空");
        }
        accountWalletMapper.insert(wallet);
        return wallet.getId();
    }

    @Override
    public AccountWalletDo getWallet(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("钱包ID不能为空");
        }
        return accountWalletMapper.selectById(id);
    }

    @Override
    public AccountWalletDo getWallet(Long userId, String currency, Integer walletType) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (Objects.isNull(currency) || currency.isEmpty()) {
            throw new IllegalArgumentException("币种不能为空");
        }
        if (walletType == null) {
            throw new IllegalArgumentException("钱包类型不能为空");
        }
        return accountWalletMapper.getByUserCurrencyType(userId, currency, walletType);
    }

    @Override
    public List<AccountWalletDo> listWallets(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return accountWalletMapper.listByUserId(userId);
    }

    @Override
    public int updateWallet(AccountWalletDo wallet) {
        if (wallet == null || wallet.getId() == null) {
            throw new IllegalArgumentException("钱包ID不能为空");
        }
        return accountWalletMapper.updateById(wallet);
    }

    @Override
    public int deleteWallet(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("钱包ID不能为空");
        }
        return accountWalletMapper.deleteById(id);
    }
}
