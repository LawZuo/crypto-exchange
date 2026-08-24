package coin.exchange.business.account.service;

import coin.exchange.business.account.domain.AccountBalanceLogDo;

import java.util.List;

/**
 * 资金变动记录服务层
 */
public interface AccountBalanceLogService {

    /**
     * 创建资金记录
     */
    Long createBalanceLog(AccountBalanceLogDo balanceLog);

    /**
     * 根据ID查询资金记录
     */
    AccountBalanceLogDo getBalanceLog(Long id);

    /**
     * 根据用户ID查询资金记录
     */
    List<AccountBalanceLogDo> listBalanceLogs(Long userId);

    /**
     * 根据关联业务查询资金记录
     */
    List<AccountBalanceLogDo> listBalanceLogs(Integer associationType, Long associationId);

    /**
     * 删除资金记录
     */
    int deleteBalanceLog(Long id);
}
