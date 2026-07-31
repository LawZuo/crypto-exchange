package coin.exchange.module.mail.service.impl;

import coin.exchange.module.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendTextEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
            log.info("纯文本邮件发送成功 -> {}", to);
        } catch (MailException e) {
            log.error("邮件发送失败 -> {}, error: {}", to, e.getMessage(), e);
            throw e; // 根据业务决定是抛出还是吞掉
        }

    }

    @Override
    public void sendHtmlEmail(String to, String subject, Map<String, Object> variables) {

    }
}
