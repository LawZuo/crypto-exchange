package coin.exchange.api.account.dto;

import coin.exchange.common.core.enums.WalletAssetsCode;
import coin.exchange.common.core.enums.WalletTypeCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 冻结资产
 */
@Data
public class AccountFrozenAssetsDto {

    // 用户id
    private Long userId;

    // 操作币种
    private String currency;

    // 交易币种价格
    private BigDecimal price;

    // 钱包类型
    private WalletTypeCode walletType; // 1资产钱包 2现货钱包 3合约钱包

    // 数量
    private BigDecimal quantity;

    // 操作类型
    private WalletAssetsCode operationType;

    // 关联类型
    private int associationType;

    // 关联的订单id
    private String associationId;
}
