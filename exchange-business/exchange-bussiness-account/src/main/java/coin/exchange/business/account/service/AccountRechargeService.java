package coin.exchange.business.account.service;

import cn.hutool.db.PageResult;
import coin.exchange.business.account.domain.AccountRechargeRecordDo;
import coin.exchange.common.core.vo.PageResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * 充值服务
 */
public interface AccountRechargeService extends IService<AccountRechargeRecordDo> {
    /**
     * TODO 用户层
     * 1. 用户充值申请
     * 2. 用户查看充值记录
     */

    /**
     * 用户充值申请
     * @param userId {用户ID}
     * @param currency {充值币种}
     * @param amount {充值金额}
     * @return 充值记录ID
     */
    Long applyRecharge(Long userId, String currency, BigDecimal amount);

    /**
     * 分页返回充值记录
     * @param userId {用户ID}
     * @return 充值记录
     */
    PageResultVo<AccountRechargeRecordDo> listRechargeRecords(
            Integer pageNum,
            Integer pageSize,
            Long userId,
            String uid,
            String currency
    );


    /**
     * TODO admin层
     * 1. 充值记录
     * 2. 充值审核
     */
}
