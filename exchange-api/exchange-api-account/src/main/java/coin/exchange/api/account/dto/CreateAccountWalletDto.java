package coin.exchange.api.account.dto;

import coin.exchange.common.core.enums.WalletTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountWalletDto {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "币种不能为空")
    private String currency;

    @NotNull(message = "钱包类型不能为空")
    private WalletTypeCode walletType;
}
