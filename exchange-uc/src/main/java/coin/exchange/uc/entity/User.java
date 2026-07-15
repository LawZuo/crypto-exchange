package coin.exchange.uc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @TableField("uid")
    private String uid;

    @TableField("username")
    private String username;

    @TableField("name")
    private String name;

    @TableField("email")
    private String email;

    @TableField(select = false) //
    private String password;

    @TableField("trade_password")
    private String tradePassword;


    @TableField("status")
    private String status;

    @TableField("kyc_status")
    private String kycStatus;

    @TableField("last_login_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_ip")
    private String registerIp;

    @TableField("created_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdTime;

    @TableField("update_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;
}
