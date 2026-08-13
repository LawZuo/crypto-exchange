package coin.exchange.business.notification.mq;

import coin.exchange.api.notification.model.NotificationEventDto;
import coin.exchange.common.core.constant.MqConstants;
import coin.exchange.common.rabbitmq.model.MqMessage;
import coin.exchange.business.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MqConstants.NOTIFICATION_QUEUE)
    public void onNotificationEvent(MqMessage<?> message,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            NotificationEventDto event = objectMapper.convertValue(message.getPayload(), NotificationEventDto.class);
            notificationService.handleEvent(event);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("【exchange-business-notification】消费通知MQ失败: messageId={}",
                    message == null ? null : message.getMessageId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
