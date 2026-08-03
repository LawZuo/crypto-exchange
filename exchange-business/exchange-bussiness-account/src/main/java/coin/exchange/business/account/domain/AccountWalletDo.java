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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("currency")
    private String currency;

    @TableField("wallet_type")
    private Integer walletType;

    @TableField("available_balance")
    private BigDecimal availableBalance;

    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    @TableField("total_balance")
    private BigDecimal totalBalance;

    @TableField("address")
    private String address;

    @TableField("network")
    private String network;

    @TableField("public_key")
    private String publicKey;

    @TableField("private_key")
    private String privateKey;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

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
