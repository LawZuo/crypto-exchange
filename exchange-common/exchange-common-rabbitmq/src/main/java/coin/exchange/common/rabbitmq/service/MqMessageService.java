package coin.exchange.common.rabbitmq.service;

import coin.exchange.common.rabbitmq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MqMessageService {

    private final RabbitTemplate rabbitTemplate;

    public <T> void send(String exchange, String routingKey, String bizType, T payload) {
        MqMessage<T> message = MqMessage.<T>builder()
                .messageId(UUID.randomUUID().toString())
                .bizType(bizType)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();

        CorrelationData cd = new CorrelationData(message.getMessageId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, cd);
    }
}
