package coin.exchange.business.market.mq;

import coin.exchange.api.market.model.MarketStreamMessageVo;
import coin.exchange.business.market.service.MarketCacheService;
import coin.exchange.business.market.ws.MarketWebSocketPublisher;
import coin.exchange.common.core.constant.MqConstants;
import coin.exchange.common.rabbitmq.model.MqMessage;
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
public class MarketDataConsumer {

    private final MarketCacheService marketCacheService;
    private final MarketWebSocketPublisher marketWebSocketPublisher;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MqConstants.MARKET_DATA_QUEUE)
    public void onMarketData(MqMessage<?> message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            MarketStreamMessageVo streamMessage = objectMapper.convertValue(message.getPayload(), MarketStreamMessageVo.class);
            marketCacheService.update(streamMessage);
            marketWebSocketPublisher.publish(streamMessage);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("【exchange-business-market】消费行情MQ失败: messageId={}", message == null ? null : message.getMessageId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
