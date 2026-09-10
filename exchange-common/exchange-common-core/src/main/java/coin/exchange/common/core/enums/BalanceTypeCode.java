package coin.exchange.common.core.enums;

import lombok.Getter;

/**
 * 资金类型
 */
@Getter
public enum BalanceTypeCode {
    AVAILABLE(1, "可用余额"),
    FROZEN(2, "冻结余额");

    private final int code;
    private final String message;

    BalanceTypeCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
