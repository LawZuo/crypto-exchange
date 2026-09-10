package coin.exchange.common.core.enums;

import lombok.Getter;

@Getter
public enum WalletTypeCode {

    ASSETS(0, "资产钱包"),
    SPOT(1, "现货钱包");

    private final int code;
    private final String message;

    WalletTypeCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
