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
 * 账户钱包表
 */
@Data
@TableName("account_wallet")
public class AccountWalletDo {

    // 主键ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户ID
    @TableField("user_id")
    private Long userId;

    // 币种
    @TableField("currency")
    private String currency;

    // 钱包类型
    @TableField("wallet_type")
    private Integer walletType;

    // 可用余额
    @TableField("available_balance")
    private BigDecimal availableBalance;

    // 冻结余额
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    // 总余额
    @TableField("total_balance")
    private BigDecimal totalBalance;

    // 地址
    @TableField("address")
    private String address;

    // 网络
    @TableField("network")
    private String network;

    // 公钥
    @TableField("public_key")
    private String publicKey;

    // 私钥
    @TableField("private_key")
    private String privateKey;

    // 状态
    @TableField("status")
    private Integer status;

    // 备注
    @TableField("remark")
    private String remark;

    // 版本
    @TableField("version")
    private Integer version;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
