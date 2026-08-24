package coin.exchange.module.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户表
 */

@Data
@TableName("user_information")
public class UserDo {

    // 用户Id
    @TableId(value = "id", type = IdType.AUTO) // 插入数据后自动返回userId
    private Long id;

    // 用户uid
    @TableField("uid")
    private String uid;

    // 用户名
    @TableField("username")
    private String username;

    // 姓名
    @TableField("name")
    private String name;

    // 邮箱
    @TableField("email")
    private String email;

    // 密码
    // @TableField(select = false) // 不扫描字段
    @TableField("password")
    private String password;

    // 交易密码
    @TableField("trade_password")
    private String tradePassword;

    // 状态
    @TableField("status")
    private int status;

    // KYC状态
    @TableField("kyc_status")
    private int kycStatus;

    // 最后登录时间
    @TableField("last_login_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    // 最后登录IP
    @TableField("last_login_ip")
    private String lastLoginIp;

    // 注册IP
    @TableField("register_ip")
    private String registerIp;

    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // @TableField(value = "create_time", fill = FieldFill.INSERT)
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdTime;

    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 对 JSON 请求体完全无效！
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    // @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;
}
