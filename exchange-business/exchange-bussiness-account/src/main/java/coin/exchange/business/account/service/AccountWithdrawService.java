package coin.exchange.business.account.service;

import coin.exchange.business.account.domain.AccountWithdrawRecordDo;
import coin.exchange.common.core.vo.PageResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 提现服务
 */
public interface AccountWithdrawService extends IService<AccountWithdrawRecordDo> {

    /**
     * 分页返回提现记录。
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param userId   用户ID
     * @param currency 币种
     * @return 提现记录
     */
    PageResultVo<AccountWithdrawRecordDo> listWithdrawRecords(
            Integer pageNum,
            Integer pageSize,
            Long userId,
            String currency
    );
}
