package coin.exchange.common.rabbitmq.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ服务
 */
public class RabbitMqService {

    private static final String X_DELAY_HEADER = "x-delay";

    private final AmqpTemplate amqpTemplate;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqService(AmqpTemplate amqpTemplate, RabbitTemplate rabbitTemplate) {
        this.amqpTemplate = amqpTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送消息到队列
     *
     * @param queueName 队列名称
     * @param message 消息内容
     */
    public void sendToQueue(String queueName, Object message) {
        amqpTemplate.convertAndSend(queueName, message);
    }

    /**
     * 发送消息到交换机
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    public void convertAndSend(String exchange, String routingKey, Object message) {
        amqpTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送带确认数据的消息到交换机
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param correlationData 消息确认数据
     */
    public void convertAndSend(String exchange, String routingKey, Object message, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    /**
     * 发送经过处理的消息到交换机
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param messagePostProcessor 消息处理器
     */
    public void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor) {
        amqpTemplate.convertAndSend(exchange, routingKey, message, messagePostProcessor);
    }

    /**
     * 发送延迟消息，需要 RabbitMQ 延迟消息插件和 x-delayed-message 交换机支持。
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param delayMillis 延迟毫秒数
     */
    public void convertAndSendDelay(String exchange, String routingKey, Object message, long delayMillis) {
        convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setHeader(X_DELAY_HEADER, delayMillis);
            return msg;
        });
    }

    /**
     * 发送请求并等待响应
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param responseType 响应类型
     * @return 响应内容
     */
    public <T> T convertSendAndReceive(String exchange, String routingKey, Object message, Class<T> responseType) {
        Object response = amqpTemplate.convertSendAndReceive(exchange, routingKey, message);
        return responseType.cast(response);
    }

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }
}
