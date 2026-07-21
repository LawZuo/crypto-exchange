package coin.exchange.module.user.utils;

import coin.exchange.api.user.dto.KycApplicationDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import org.springframework.util.StringUtils;

public class ValidationUtil {

    /**
     * 用户验证器
     */
    public static void validateUser(RegisterUserDto registerUser) {
        // 越界判断
        if (registerUser == null) {
            throw new RuntimeException("用户参数不能为空");
        }
        if (registerUser.getUsername() == null) {
            throw new RuntimeException("账号不能为空");
        }
        if (registerUser.getEmail() == null) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (registerUser.getPassword() == null) {
            throw new RuntimeException("登录密码不能为空");
        }
    }

    /**
     * Kyc验证器
     */
    public static void validateKycApplication(KycApplicationDto kycApplication) {
        // 越界判断
        if (kycApplication == null) {
            throw new RuntimeException("KYC申请参数不能为空");
        }
        if (kycApplication.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (!StringUtils.hasText(kycApplication.getName())) {
            throw new RuntimeException("用户姓名不能为空");
        }
        if (!StringUtils.hasText(kycApplication.getIdCard())) {
            throw new RuntimeException("用户身份证号不能为空");
        }
        if (!StringUtils.hasText(kycApplication.getIdCardFront())) {
            throw new RuntimeException("用户身份证正面不能为空");
        }
        if (!StringUtils.hasText(kycApplication.getIdCardBack())) {
            throw new RuntimeException("用户身份证反面不能为空");
        }
    }
}
