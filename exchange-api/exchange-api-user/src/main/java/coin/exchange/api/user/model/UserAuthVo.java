package coin.exchange.api.user.model;

import lombok.Data;

/**
 * 用户认证信息。
 */
@Data
public class UserAuthVo extends UserVo {
    String password;
}
