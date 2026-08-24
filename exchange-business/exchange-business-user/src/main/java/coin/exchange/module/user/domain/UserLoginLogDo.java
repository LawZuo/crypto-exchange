package coin.exchange.module.user.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志表。
 */
@Data
@TableName("user_login_log")
public class UserLoginLogDo {

    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户ID
    @TableField("user_id")
    private Long userId;

    // IP地址
    @TableField("ip_address")
    private String ipAddress;

    // 设备来源
    @TableField("device_source")
    private String deviceSource;

    // 设备信息
    @TableField("device_info")
    private String deviceInfo;

    // 登录状态
    @TableField("status")
    private Integer status;

    // 登录失败原因
    @TableField("fail_reason")
    private String failReason;

    // 创建时间
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    // 删除状态
    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
