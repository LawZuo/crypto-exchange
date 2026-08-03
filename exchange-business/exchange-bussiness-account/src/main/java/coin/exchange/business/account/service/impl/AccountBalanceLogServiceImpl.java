package coin.exchange.business.account.service.impl;

import coin.exchange.business.account.domain.AccountBalanceLogDo;
import coin.exchange.business.account.mapper.AccountBalanceLogMapper;
import coin.exchange.business.account.service.AccountBalanceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountBalanceLogServiceImpl implements AccountBalanceLogService {

    private final AccountBalanceLogMapper accountBalanceLogMapper;

    @Override
    public Long createBalanceLog(AccountBalanceLogDo balanceLog) {
        if (balanceLog == null) {
            throw new IllegalArgumentException("资金记录不能为空");
        }
        accountBalanceLogMapper.insert(balanceLog);
        return balanceLog.getId();
    }

    @Override
    public AccountBalanceLogDo getBalanceLog(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资金记录ID不能为空");
        }
        return accountBalanceLogMapper.selectById(id);
    }

    @Override
    public List<AccountBalanceLogDo> listBalanceLogs(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return accountBalanceLogMapper.listByUserId(userId);
    }

    @Override
    public List<AccountBalanceLogDo> listBalanceLogs(Integer associationType, Long associationId) {
        if (associationType == null) {
            throw new IllegalArgumentException("关联类型不能为空");
        }
        if (associationId == null) {
            throw new IllegalArgumentException("关联ID不能为空");
        }
        return accountBalanceLogMapper.listByAssociation(associationType, associationId);
    }

    @Override
    public int deleteBalanceLog(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资金记录ID不能为空");
        }
        return accountBalanceLogMapper.deleteById(id);
    }
}
