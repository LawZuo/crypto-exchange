package coin.exchange.business.account.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金变动记录
 */
@Data
@TableName("account_balance_log")
public class AccountBalanceLogDo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("currency")
    private String currency;

    @TableField("wallet_type")
    private Integer walletType;

    @TableField("operation_type")
    private String operationType;

    @TableField("balance_type")
    private Integer balanceType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("to_usdt")
    private BigDecimal toUsdt;

    @TableField("before_available_balance")
    private BigDecimal beforeAvailableBalance;

    @TableField("after_available_balance")
    private BigDecimal afterAvailableBalance;

    @TableField("before_frozen_balance")
    private BigDecimal beforeFrozenBalance;

    @TableField("after_frozen_balance")
    private BigDecimal afterFrozenBalance;

    @TableField("association_type")
    private Integer associationType;

    @TableField("association_id")
    private Long associationId;

    @TableField("client_ip")
    private String clientIp;

    @TableField("is_hidden_user")
    private Integer isHiddenUser;

    @TableField("version")
    private Integer version;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
