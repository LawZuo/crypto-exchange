package coin.exchange.module.mail.controller;


import coin.exchange.module.mail.model.EmailCodeVo;
import coin.exchange.module.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 邮件验证码
 */
@Slf4j
@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
public class VerificationCodeEmailController {

    private final EmailService emailService;

    /**
     * 发送验证码
     */
    @PostMapping("/verification/code")
    public void sendVerificationCodeEmail(
            @RequestBody EmailCodeVo emailCodeVo
            ) {
        String email = emailCodeVo.getEmail();
        if (!StringUtils.hasText(emailCodeVo.getEmail())) {
            log.error("邮箱错误 -> {}", emailCodeVo);
            return;
        }

        String code = UUID.randomUUID().toString().substring(0, 6);
        String content = "验证码：" + code;
        String subject = "【lawzhuo】您的验证码是 " + code;

        emailService.sendTextEmail(email, subject, content);
        log.info("验证码发送成功 -> {}:{}", email, code);
    }
}
