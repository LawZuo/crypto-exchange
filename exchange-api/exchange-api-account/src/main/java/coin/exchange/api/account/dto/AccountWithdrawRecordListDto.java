package coin.exchange.api.account.dto;

import coin.exchange.common.core.dto.PageBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提现记录分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountWithdrawRecordListDto extends PageBaseDto {

    private String currency;
}
