package coin.exchange.api.user.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {
    Long id;
    String uid;
    String username;
    String name;
    String email;
    int status;
    int kycStatus;
    LocalDateTime lastLoginTime;
    String lastLoginIp;
    String registerIp;
    LocalDateTime createdTime;
    LocalDateTime updateTime;
}
