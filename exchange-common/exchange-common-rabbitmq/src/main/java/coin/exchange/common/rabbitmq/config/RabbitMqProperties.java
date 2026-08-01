package coin.exchange.common.rabbitmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "exchange.rabbitmq")
public class RabbitMqProperties {

    private List<QueueConfig> queues = new ArrayList<>();

    @Data
    public static class QueueConfig {
        private String name;
        private String exchange;
        private String routingKey;
        private String dlxExchange;
        private String dlxRoutingKey;
        private String dlxQueue;
    }
}
