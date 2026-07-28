package coin.exchange.api.user.model;

import lombok.Data;

@Data
public class LoginVo {
    private String id;
    private String username;
    private String token;
    private String expireTime;
    private String loginTime;

    private UserVo user;
}
