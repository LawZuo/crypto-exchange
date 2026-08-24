package coin.exchange.business.account.domain;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "提现记录表")
@Data
@TableName("account_withdraw_records")
public class AccountWithdrawRecordDo {

    @Schema(name = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(name = "订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(name = "币种")
    @TableField("currency")
    private String currency;

    @Schema(name = "提现金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(name = "汇率")
    @TableField("rate")
    private BigDecimal rate;

    @Schema(name = "USDT价值")
    @TableField("usdt_value")
    private BigDecimal usdtValue;

    @Schema(name = "扣除手续费金额")
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @Schema(name = "扣除手续费币种")
    @TableField("fee_currency")
    private String feeCurrency;

    @Schema(name = "扣除手续费USDT价值")
    @TableField("fee_amount_usdt")
    private BigDecimal feeAmountUsdt;

    @Schema(name = "提现地址")
    @TableField("address")
    private String address;

    @Schema(name = "提现网络")
    @TableField("network")
    private String network;

    @Schema(name = "提现状态", description = "0待处理，1成功，2失败")
    @TableField("status")
    private Integer status;

    @Schema(name = "提现类型", description = "1人工打款，2uDun打款")
    @TableField("type")
    private String type;

    @Schema(name = "备注")
    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdTime;

    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;
}
