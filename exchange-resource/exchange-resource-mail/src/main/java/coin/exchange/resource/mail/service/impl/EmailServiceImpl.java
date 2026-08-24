package coin.exchange.resource.mail.service.impl;

import coin.exchange.common.core.constant.MqConstants;
import coin.exchange.common.core.constant.RedisKeyConstants;
import coin.exchange.common.rabbitmq.model.MqMessage;
import coin.exchange.common.rabbitmq.service.MqMessageService;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.resource.mail.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final RedisService redisService;
    private final MqMessageService mqMessageService;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendTextEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mqMessageService.send(
                MqConstants.EMAIL_SEND_EXCHANGE,
                MqConstants.EMAIL_SEND_ROUTING_KEY,
                MqConstants.EMAIL_SEND_TYPE,
                message
        );
    }

    @RabbitListener(queues = MqConstants.EMAIL_SEND_QUEUE)
    public void onEmailSend(MqMessage<SimpleMailMessage> message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("开始处理邮件发送任务 -> {}", message.getMessageId());
            Boolean isNew = redisService.setIfAbsent(
                    RedisKeyConstants.MQ_MESSSAGE_KEY + message.getMessageId(),
                    "1",
                    24,
                    TimeUnit.HOURS
            );
            if (Boolean.FALSE.equals(isNew)) {
                channel.basicAck(tag, false);
                return;
            }

            try {
                SimpleMailMessage mailMessage = message.getPayload();
                mailSender.send(mailMessage);
                log.info("纯文本邮件发送成功 -> {}", (Object) mailMessage.getTo());
            } catch (MailException e) {
                log.error("邮件发送失败 -> {}, error: {}", message.getPayload().getTo(), e.getMessage(), e);
                throw e;
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("邮件发送失败, messageId={}", message.getMessageId(), e);
            channel.basicNack(tag, false, false);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, Map<String, Object> variables) {
    }
}
