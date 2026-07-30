package coin.exchange.api.user.dto;

import lombok.Data;

@Data
public class LoginRecordDto {
    private Long userId;
    private String loginIp;
    private String deviceSource;
    private String deviceInfo;
}
