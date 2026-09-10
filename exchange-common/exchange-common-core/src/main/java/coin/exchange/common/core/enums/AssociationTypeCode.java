package coin.exchange.common.core.enums;

import lombok.Getter;

/**
 * 钱包操作关联类型
 */
@Getter
public enum AssociationTypeCode {

    RECHARGE(1, "如今"),
    CASH(2, "出金"),
    TRANSLATE(3, "划转"),
    SPOT_TRADE(4, "现货交易");

    private final int code;
    private final String message;

    AssociationTypeCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
