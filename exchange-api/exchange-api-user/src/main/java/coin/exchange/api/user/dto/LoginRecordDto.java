package coin.exchange.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRecordDto {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "登录IP不能为空")
    private String loginIp;

    private String deviceSource;

    private String deviceInfo;
}
