package coin.exchange.module.datasrouce.config;

import coin.exchange.common.core.constant.MqConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketRabbitConfig {

    @Value("${exchange.rabbitmq.market-data-exchange}")
    private String exchangeName;

    @Bean
    public TopicExchange marketDataExchange() {
        return new TopicExchange(exchangeName, true, false);
    }
}
