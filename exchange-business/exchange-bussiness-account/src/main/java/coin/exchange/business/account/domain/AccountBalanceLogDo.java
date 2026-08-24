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

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户id
    @TableField("user_id")
    private Long userId;

    // 币种
    @TableField("currency")
    private String currency;

    // 钱包类型
    @TableField("wallet_type")
    private Integer walletType;

    // 操作类型
    @TableField("operation_type")
    private String operationType;

    // 资金类型
    @TableField("balance_type")
    private Integer balanceType;

    // 金额
    @TableField("amount")
    private BigDecimal amount;

    // 兑换USDT
    @TableField("to_usdt")
    private BigDecimal toUsdt;

    // 账户可用余额
    @TableField("before_available_balance")
    private BigDecimal beforeAvailableBalance;

    // 账户冻结余额
    @TableField("after_available_balance")
    private BigDecimal afterAvailableBalance;

    // 账户总余额
    @TableField("before_frozen_balance")
    private BigDecimal beforeFrozenBalance;

    // 账户总余额
    @TableField("after_frozen_balance")
    private BigDecimal afterFrozenBalance;

    // 关联类型
    @TableField("association_type")
    private Integer associationType;

    // 关联id
    @TableField("association_id")
    private Long associationId;

    // 客户端ip
    @TableField("client_ip")
    private String clientIp;

    // 是否隐藏用户
    @TableField("is_hidden_user")
    private Integer isHiddenUser;

    // 版本
    @TableField("version")
    private Integer version;

    // 备注
    @TableField("remark")
    private String remark;

    // 创建时间
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    // 修改时间
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    // 删除标识
    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
