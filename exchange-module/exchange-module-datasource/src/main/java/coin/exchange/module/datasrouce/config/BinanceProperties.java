package coin.exchange.module.datasrouce.config;

import coin.exchange.module.datasrouce.enums.BinanceStreamType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "exchange.market.binance")
public class BinanceProperties {
    // 是否启动币安行情 WebSocket 连接。
    private boolean enabled = false;

    // 币安现货行情 WebSocket 根地址。
    private String baseUrl;

    // 币安现货行情 REST 根地址。
    private String restBaseUrl = "https://api.binance.com";

    // 订阅的数据流类型。
    private List<BinanceStreamType> streamTypes = new ArrayList<>(List.of(BinanceStreamType.TICKER));;

    // K 线周期，仅在 streamTypes 包含 KLINE 时使用。
    private String klineInterval = "1m";

    // 连接断开后的重连延迟。
    private Duration reconnectDelay = Duration.ofSeconds(5);

    // 行情持久化间隔配置预留，由消费方按需使用
    private Duration persistInterval = Duration.ofSeconds(1);

    // 超时
    private int timeoutSeconds = 30;

}
