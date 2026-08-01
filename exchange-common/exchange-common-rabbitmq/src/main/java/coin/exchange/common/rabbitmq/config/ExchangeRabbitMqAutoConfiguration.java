package coin.exchange.common.rabbitmq.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * RabbitMQ自动配置
 */
@Slf4j
@EnableRabbit
@AutoConfiguration(after = RabbitAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, RabbitTemplate.class})
@EnableConfigurationProperties(RabbitMqProperties.class)
public class ExchangeRabbitMqAutoConfiguration {

    @Bean("exchangeRabbitObjectMapper")
    @ConditionalOnMissingBean(name = "exchangeRabbitObjectMapper")
    public ObjectMapper exchangeRabbitObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);

        // 开启发送确认（保证消息到达交换机）
        template.setConfirmCallback((data, ack, cause) -> {
            if (!ack) {
                log.error("消息未到达交换机! correlationData={}, cause={}", data, cause);
                // TODO: 落库重试或告警
            }
        });

        // 开启返回回调（保证消息从交换机路由到队列）
        template.setReturnsCallback(returned -> {
            log.warn("消息未路由到队列! replyCode={}, routingKey={}",
                    returned.getReplyCode(), returned.getRoutingKey());
        });

        template.setMandatory(true);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    @ConditionalOnProperty(prefix = "exchange.rabbitmq", name = "json-message-converter", havingValue = "true", matchIfMissing = true)
    public MessageConverter exchangeRabbitMessageConverter(@Qualifier("exchangeRabbitObjectMapper") ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        // 关键：防止反序列化时因缺少 __TypeId__ 头而失败
        converter.setCreateMessageIds(true);
        return converter;
    }

    /**
     * 根据配置文件动态声明交换机和队列
     * 业务方只需在 yml 中配置，无需写 @Bean
     */
    @Bean
    public Declarables dynamicDeclarables(RabbitMqProperties properties) {
        List<Declarable> declarables = new ArrayList<>();
        properties.getQueues().forEach(q -> {
            TopicExchange exchange = new TopicExchange(q.getExchange(), true, false);
            Queue queue = QueueBuilder.durable(q.getName())
                    .withArgument("x-dead-letter-exchange", q.getDlxExchange())
                    .withArgument("x-dead-letter-routing-key", q.getDlxRoutingKey())
                    .build();
            Binding binding = BindingBuilder.bind(queue).to(exchange)
                    .with(q.getRoutingKey());

            // 死信交换机与队列
            TopicExchange dlxExchange = new TopicExchange(q.getDlxExchange(), true, false);
            Queue dlxQueue = QueueBuilder.durable(q.getDlxQueue()).build();
            Binding dlxBinding = BindingBuilder.bind(dlxQueue).to(dlxExchange)
                    .with(q.getDlxRoutingKey());

            declarables.addAll(List.of(exchange, queue, binding, dlxExchange, dlxQueue, dlxBinding));
        });
        return new Declarables(declarables);
    }
}
