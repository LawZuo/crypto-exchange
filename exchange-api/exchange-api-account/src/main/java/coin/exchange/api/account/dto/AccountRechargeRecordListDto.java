package coin.exchange.api.account.dto;

import coin.exchange.common.core.dto.PageBaseDto;
import lombok.Data;

@Data
public class AccountRechargeRecordListDto extends PageBaseDto {

    private String currency;
}
