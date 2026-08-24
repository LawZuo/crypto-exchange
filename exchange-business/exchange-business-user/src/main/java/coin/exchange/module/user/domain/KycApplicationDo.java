package coin.exchange.module.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户KYC申请表
 */
@Data
@TableName("user_kyc_application")
public class KycApplicationDo {

    // id
    @TableId(value = "id", type = IdType.AUTO) // 插入数据后自动返回Id
    private Long id;

    // 用户id
    @TableField("user_id")
    private Long userId;

    // 姓名
    @TableField("name")
    private String name;

    // 身份证号码
    @TableField("id_card")
    private String idCard;

    // 身份证正面
    @TableField("id_card_front")
    private String idCardFront;

    // 身份证反面
    @TableField("id_card_back")
    private String idCardBack;

    // 状态
    @TableField("status")
    private int status;

    // 创建时间
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdTime;

    // 更新时间
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    // 软删除
    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;
}
