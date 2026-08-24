package coin.exchange.business.account.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录表
 */

@Data
@TableName("account_recharge_records")
public class AccountRechargeRecordDo {

    // 充值记录ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 订单号
    @TableField("order_no")
    private String orderNo;

    // 用户ID
    @TableField("user_id")
    private Long userId;

    // 币种
    @TableField("currency")
    private String currency;

    // 充值金额
    @TableField("amount")
    private BigDecimal amount;

    // 汇率
    @TableField("rate")
    private BigDecimal rate;

    // USDT价值
    @TableField("usdt_value")
    private BigDecimal usdtValue;

    // 充值类型 MANUAL常规充值，ADDRESS地址充值
    @TableField("type")
    private String type;

    // 充值分类 1-常规，2-放款，3-还款，4-彩金，5-其他，6-uDun
    @TableField("category")
    private Integer category;

    // 充值状态 0待处理，1成功，2失败
    @TableField("status")
    private Integer status;

    // 充值地址
    @TableField("address")
    private String address;

    // 备注
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
