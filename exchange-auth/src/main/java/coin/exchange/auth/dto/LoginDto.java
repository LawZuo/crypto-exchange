package coin.exchange.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {
    @Size(max = 64, message = "账号长度不能超过64个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    /** 1-账号登录，2-邮箱登录 */
    @Min(value = 1, message = "登录类型只支持1-账号或2-邮箱")
    @Max(value = 2, message = "登录类型只支持1-账号或2-邮箱")
    private int loginType;

    @jakarta.validation.constraints.NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 64, message = "登录密码长度必须在6到64个字符之间")
    private String password;
}
