package coin.exchange.common.core.constant;


/**
 * RabbitMQ消息队列类型相关通用常量
 */

public class MqConstants {

    /**
     * 邮件验证码
     */
    public static final String EMAIL_SEND_TYPE = "EMAIL_SEND";
    public static final String EMAIL_SEND_NAME = "exchange.email.send";
    public static final String EMAIL_SEND_KEY = "queue.email.send";

    /**
     * 行情广播
     */
    public static final String MARKET_DATA_EXCHANGE = "exchange.market.data";
    public static final String MARKET_DATA_QUEUE = "queue.market.data.business";
    public static final String MARKET_DATA_ROUTING_KEY = "market.binance.#";
    public static final String MARKET_DATA_DLX_EXCHANGE = "exchange.market.data.dlx";
    public static final String MARKET_DATA_DLX_ROUTING_KEY = "market.binance.dlx";
    public static final String MARKET_DATA_DLX_QUEUE = "queue.market.data.business.dlx";
    public static final String MARKET_DATA_BIZ_TYPE = "MARKET_DATA";

    public static final String MARKET_DATA_ROUTING_PREFIX = "market.binance.";

    /**
     * 用户私有化通知
     */
    public static final String NOTIFICATION_EXCHANGE = "exchange.notification";
    public static final String NOTIFICATION_QUEUE = "queue.notification.business";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.event.#";
    public static final String NOTIFICATION_EVENT_ROUTING_KEY = "notification.event.common";
    public static final String NOTIFICATION_DLX_EXCHANGE = "exchange.notification.dlx";
    public static final String NOTIFICATION_DLX_ROUTING_KEY = "notification.event.dlx";
    public static final String NOTIFICATION_DLX_QUEUE = "queue.notification.business.dlx";
    public static final String NOTIFICATION_BIZ_TYPE = "NOTIFICATION_EVENT";

}
