package coin.exchange.module.datasrouce.utils;

import coin.exchange.module.datasrouce.config.BinanceProperties;
import coin.exchange.module.datasrouce.enums.BinanceStreamType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BinanceUtils {

    /**
     * 构建订阅流
     * @param streamTypes 订阅的类型 ticker，trade，kline
     * @param symbols 订阅的币种列表
     * @parma klineInterval kline的间隔 1m 5m 15m 30m 1h 2h 4h 6h 8h 12h 1d 3d 1w 1M
     * @return
     */
    public static List<String> buildStreams(
            List<BinanceStreamType> streamTypes,
            List<String> symbols,
            String klineInterval
    ) {
        if (streamTypes == null || streamTypes.isEmpty()) {
            return List.of();
        }
        return symbols.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(symbol -> !symbol.isBlank())
                .map(symbol -> symbol.toLowerCase(Locale.ROOT))
                .flatMap(symbol -> streamTypes.stream().map(streamType -> buildStream(symbol, streamType, klineInterval)))
                .toList();
    }

    public static String buildStream(String symbol, BinanceStreamType streamType, String klineInterval) {
        if (streamType == BinanceStreamType.KLINE) {
            return symbol + "@kline_" + klineInterval;
        }
        if (streamType == BinanceStreamType.DEPTH) {
            return symbol + "@depth@100ms";
        }
        return symbol + "@" + streamType.getStreamName();
    }
}
