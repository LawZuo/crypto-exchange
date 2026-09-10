package coin.exchange.common.core.constant;

public class RedisKeyConstants {

    /**
     * MQ消息队列key
     */
    public static final String MQ_MESSSAGE_KEY = "mq:dedup:";

    /**
     * 用户登录Token
     */
    public static final String LOGIN_TOKEN_KEY_PREFIX = "user:login:";

    /**
     * 币种最新数据
     */
    public static final String KLINE_KEY_PREFIX = "kline:latest:";
}
