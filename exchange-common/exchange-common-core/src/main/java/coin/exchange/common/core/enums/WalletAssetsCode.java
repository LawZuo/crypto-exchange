package coin.exchange.common.core.enums;

import lombok.Getter;

/**
 * 钱包资金操作类型
 */
@Getter
public enum WalletAssetsCode {


    /** 现货 **/
    SPOT_FROZEN_ASSETS("SPOT_FROZEN_ASSETS", "现货交易冻结资产"),
    SPOT_FROZEN_FEE("SPOT_FROZEN_FEE", "现货交易冻结手续费"),

    SPOT_DEDUCT_ASSETS("SPOT_DEDUCT_ASSETS", "现货交易扣除资产"),
    SPOT_DEDUCT_FEE("SPOT_DEDUCT_FEE", "现货交易扣除手续费");



    private final String code;
    private final String message;

    WalletAssetsCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
