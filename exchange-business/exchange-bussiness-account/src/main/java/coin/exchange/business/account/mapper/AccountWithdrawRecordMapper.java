package coin.exchange.business.account.mapper;

import coin.exchange.business.account.domain.AccountWithdrawRecordDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提现记录Mapper
 */

@Mapper
public interface AccountWithdrawRecordMapper extends BaseMapper<AccountWithdrawRecordDo> {
}
