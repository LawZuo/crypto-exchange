package coin.exchange.module.datasrouce.mq;

import coin.exchange.api.market.model.MarketStreamMessageVo;
import coin.exchange.common.core.constant.MqConstants;
import coin.exchange.common.rabbitmq.service.MqMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataPublisher {

    private final MqMessageService mqMessageService;

    @Value("${exchange.rabbitmq.market-data-exchange}")
    private String marketDataExchange;

    @Value("${exchange.rabbitmq.market-data-biz-type}")
    private String marketDataBizType;

    public void publish(String type, String symbol, String interval, Object payload) {
        MarketStreamMessageVo message = new MarketStreamMessageVo();
        message.setSource("binance");
        message.setType(type);
        message.setSymbol(normalizeSymbol(symbol));
        message.setInterval(interval);
        message.setPayload(payload);
        message.setTimestamp(System.currentTimeMillis());

        try {
            mqMessageService.send(marketDataExchange, routingKey(type, symbol), marketDataBizType, message);
        } catch (Exception e) {
            log.warn("发布行情MQ失败: type={}, symbol={}, error={}", type, symbol, e.getMessage());
        }
    }

    private String routingKey(String type, String symbol) {
        return MqConstants.MARKET_DATA_ROUTING_PREFIX + type + "." + normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
    }
}
