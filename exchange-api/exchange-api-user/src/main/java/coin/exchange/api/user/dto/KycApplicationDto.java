package coin.exchange.api.user.dto;

import lombok.Data;

/**
 * KYC申请参数
 */
@Data
public class KycApplicationDto {
    private String name;
    private String idCard;
    private String idCardFront;
    private String idCardBack;
    private Long userId;
}
