package coin.exchange.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * KYC申请参数
 */
@Data
public class KycApplicationDto {
    @NotBlank(message = "用户姓名不能为空")
    @Size(max = 64, message = "用户姓名长度不能超过64个字符")
    private String name;

    @NotBlank(message = "用户身份证号不能为空")
    @Size(max = 32, message = "用户身份证号长度不能超过32个字符")
    private String idCard;

    @NotBlank(message = "用户身份证正面不能为空")
    @Size(max = 255, message = "用户身份证正面长度不能超过255个字符")
    private String idCardFront;

    @NotBlank(message = "用户身份证反面不能为空")
    @Size(max = 255, message = "用户身份证反面长度不能超过255个字符")
    private String idCardBack;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
