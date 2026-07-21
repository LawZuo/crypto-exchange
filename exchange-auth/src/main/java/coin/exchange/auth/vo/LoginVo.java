package coin.exchange.auth.vo;

import coin.exchange.api.user.model.UserVo;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应数据
 */
@Data
public class LoginVo {
    private String token;
    private UserVo user;
    private String expireTime;
    private String loginTime;
}
