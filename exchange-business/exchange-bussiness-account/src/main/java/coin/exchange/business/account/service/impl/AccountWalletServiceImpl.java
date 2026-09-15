package coin.exchange.business.account.service.impl;

import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.api.account.dto.CreateAccountWalletDto;
import coin.exchange.business.account.domain.AccountBalanceLogDo;
import coin.exchange.business.account.domain.AccountWalletDo;
import coin.exchange.business.account.mapper.AccountWalletMapper;
import coin.exchange.business.account.service.AccountBalanceLogService;
import coin.exchange.business.account.service.AccountWalletService;
import coin.exchange.common.core.enums.WalletTypeCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountWalletServiceImpl implements AccountWalletService {

    private final AccountWalletMapper accountWalletMapper;
    private final AccountBalanceLogService accountBalanceLogService;

    @Override
    public Long createWallet(AccountWalletDo wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("钱包信息不能为空");
        }
        accountWalletMapper.insert(wallet);
        return wallet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getOrCreateWallet(CreateAccountWalletDto walletDto) {
        if (walletDto == null || walletDto.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (walletDto.getCurrency() == null || walletDto.getCurrency().isBlank()) {
            throw new IllegalArgumentException("币种不能为空");
        }
        if (walletDto.getWalletType() == null) {
            throw new IllegalArgumentException("钱包类型不能为空");
        }

        String currency = walletDto.getCurrency().trim().toUpperCase(Locale.ROOT);
        Integer walletType = walletDto.getWalletType().getCode();
        AccountWalletDo existing = getWallet(walletDto.getUserId(), currency, walletType);
        if (existing != null) {
            return existing.getId();
        }

        AccountWalletDo wallet = new AccountWalletDo();
        wallet.setUserId(walletDto.getUserId());
        wallet.setCurrency(currency);
        wallet.setWalletType(walletType);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setTotalBalance(BigDecimal.ZERO);
        wallet.setStatus(1);
        wallet.setVersion(1);
        wallet.setIsDeleted(0);
        try {
            accountWalletMapper.insert(wallet);
            return wallet.getId();
        } catch (DuplicateKeyException duplicateKeyException) {
            AccountWalletDo concurrentWallet = getWallet(walletDto.getUserId(), currency, walletType);
            if (concurrentWallet != null) {
                return concurrentWallet.getId();
            }
            throw duplicateKeyException;
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void frozenBalance(AccountFrozenAssetsDto frozenAssetsDto) {

        // 1. 获取用户钱包
        AccountWalletDo wallet = this.getWallet(frozenAssetsDto.getUserId(), frozenAssetsDto.getCurrency(), frozenAssetsDto.getWalletType().getCode());

        if (wallet == null) {
            log.info("冻结{}资产失败：用户钱包不存在，用户ID：{}", frozenAssetsDto.getWalletType().getMessage(), frozenAssetsDto.getUserId());
            throw new RuntimeException("冻结" + frozenAssetsDto.getWalletType().getMessage() + "资产失败：用户钱包不存在");
        }

        // 2. 判断可用余额
        BigDecimal frozenQuantity = frozenAssetsDto.getQuantity();
        BigDecimal availableBalance = wallet.getAvailableBalance();

        if (frozenQuantity.compareTo(availableBalance) > 0) {
            log.info("冻结{}资产失败：可用余额不足，冻结资产：{}，可用余额：{}", frozenAssetsDto.getWalletType().getMessage(), frozenAssetsDto, availableBalance);
            throw new RuntimeException("冻结" + frozenAssetsDto.getWalletType().getMessage() + "资产失败：可用余额不足");
        }

        // 修改钱包余额
        BigDecimal oldFrozenBalance = wallet.getFrozenBalance();
        BigDecimal oldAvailableBalance = wallet.getAvailableBalance();
        BigDecimal newFrozenBalance = oldFrozenBalance.add(frozenQuantity);
        BigDecimal newAvailableBalance = oldAvailableBalance.subtract(frozenQuantity);
        BigDecimal newTotalBalance = newAvailableBalance.add(newFrozenBalance);

        // 更新钱包
        wallet.setAvailableBalance(newAvailableBalance);
        wallet.setFrozenBalance(newFrozenBalance);
        wallet.setTotalBalance(newTotalBalance);
        if (accountWalletMapper.updateById(wallet) != 1) {
            throw new IllegalStateException("冻结资产失败：钱包余额更新失败");
        }

        // 写入日志
        AccountBalanceLogDo balanceLog = new AccountBalanceLogDo();
        balanceLog.setUserId(frozenAssetsDto.getUserId());
        balanceLog.setCurrency(frozenAssetsDto.getCurrency());
        balanceLog.setWalletType(frozenAssetsDto.getWalletType().getCode());
        balanceLog.setOperationType(frozenAssetsDto.getOperationType().getCode());
        balanceLog.setAmount(frozenAssetsDto.getQuantity());
        balanceLog.setToUsdt(frozenAssetsDto.getQuantity().multiply(frozenAssetsDto.getPrice()));
        balanceLog.setBeforeFrozenBalance(oldFrozenBalance);
        balanceLog.setAfterFrozenBalance(newFrozenBalance);
        balanceLog.setBeforeAvailableBalance(oldAvailableBalance);
        balanceLog.setAfterAvailableBalance(newAvailableBalance);
        balanceLog.setAssociationType(frozenAssetsDto.getAssociationType());
        balanceLog.setAssociationId(frozenAssetsDto.getAssociationId());
        balanceLog.setVersion(1);
        balanceLog.setRemark(frozenAssetsDto.getOperationType().getMessage());
        Long logId = accountBalanceLogService.createBalanceLog(balanceLog);
        log.info("【冻结余额成功】：钱包ID:{}, 记录ID: {}", wallet.getId(), logId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(AccountFrozenAssetsDto assetsDto) {

        // 1. 获取用户钱包
        AccountWalletDo wallet = this.getWallet(assetsDto.getUserId(), assetsDto.getCurrency(), assetsDto.getWalletType().getCode());

        if (wallet == null) {
            log.info("扣除{}资产失败：用户钱包不存在，用户ID：{}", assetsDto.getWalletType().getMessage(), assetsDto.getUserId());
            throw new RuntimeException("扣除" + assetsDto.getWalletType().getMessage() + "资产失败：用户钱包不存在");
        }

        // 2. 判断可用余额
        BigDecimal deductQuantity = assetsDto.getQuantity();
        BigDecimal availableBalance = wallet.getAvailableBalance();

        if (deductQuantity.compareTo(availableBalance) > 0) {
            log.info("扣除{}资产失败：可用余额不足，扣除资产：{}，可用余额：{}", assetsDto.getWalletType().getMessage(), assetsDto, availableBalance);
            throw new RuntimeException("扣除" + assetsDto.getWalletType().getMessage() + "资产失败：可用余额不足");
        }

        // 修改钱包余额
        BigDecimal frozenBalance = wallet.getFrozenBalance();
        BigDecimal oldAvailableBalance = wallet.getAvailableBalance();
        BigDecimal newAvailableBalance = oldAvailableBalance.subtract(deductQuantity);
        BigDecimal newTotalBalance = newAvailableBalance.add(frozenBalance);

        // 更新钱包
        wallet.setAvailableBalance(newAvailableBalance);
        wallet.setFrozenBalance(frozenBalance);
        wallet.setTotalBalance(newTotalBalance);
        if (accountWalletMapper.updateById(wallet) != 1) {
            throw new IllegalStateException("扣除资产失败：钱包余额更新失败");
        }

        // 写入日志
        AccountBalanceLogDo balanceLog = new AccountBalanceLogDo();
        balanceLog.setUserId(assetsDto.getUserId());
        balanceLog.setCurrency(assetsDto.getCurrency());
        balanceLog.setWalletType(assetsDto.getWalletType().getCode());
        balanceLog.setOperationType(assetsDto.getOperationType().getCode());
        balanceLog.setAmount(assetsDto.getQuantity());
        balanceLog.setToUsdt(assetsDto.getQuantity().multiply(assetsDto.getPrice()));
        balanceLog.setBeforeFrozenBalance(frozenBalance);
        balanceLog.setAfterFrozenBalance(frozenBalance);
        balanceLog.setBeforeAvailableBalance(oldAvailableBalance);
        balanceLog.setAfterAvailableBalance(newAvailableBalance);
        balanceLog.setAssociationType(assetsDto.getAssociationType());
        balanceLog.setAssociationId(assetsDto.getAssociationId());
        balanceLog.setVersion(1);
        balanceLog.setRemark(assetsDto.getOperationType().getMessage());
        Long logId = accountBalanceLogService.createBalanceLog(balanceLog);
        log.info("【扣除余额成功】：钱包ID:{}, 记录ID: {}", wallet.getId(), logId);
    }
}
