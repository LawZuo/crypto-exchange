package coin.exchange.module.datasrouce.cache;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketMemoryCache {

    private final ConcurrentHashMap<String, BinanceTickerVo> tickerCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BinanceDepthVo> depthCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BinanceTradeVo> tradeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BinanceKlineVo> klineCache = new ConcurrentHashMap<>();

    public void putTicker(String symbol, BinanceTickerVo ticker) {
        tickerCache.put(symbolKey(symbol), ticker);
    }

    public void putDepth(String symbol, BinanceDepthVo depth) {
        depthCache.put(symbolKey(symbol), depth);
    }

    public void putTrade(String symbol, BinanceTradeVo trade) {
        tradeCache.put(symbolKey(symbol), trade);
    }

    public void putKline(String symbol, String interval, BinanceKlineVo kline) {
        klineCache.put(klineKey(symbol, interval), kline);
    }

    public Map<String, BinanceTickerVo> tickerSnapshot() {
        return Map.copyOf(tickerCache);
    }

    public Map<String, BinanceDepthVo> depthSnapshot() {
        return Map.copyOf(depthCache);
    }

    public Map<String, BinanceTradeVo> tradeSnapshot() {
        return Map.copyOf(tradeCache);
    }

    public List<KlineSnapshot> klineSnapshot() {
        return klineCache.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split(":", 2);
                    return new KlineSnapshot(parts[0], parts.length > 1 ? parts[1] : "1m", entry.getValue());
                })
                .toList();
    }

    private String symbolKey(String symbol) {
        return symbol.trim().toLowerCase(Locale.ROOT);
    }

    private String klineKey(String symbol, String interval) {
        return symbolKey(symbol) + ":" + interval.trim();
    }

    public record KlineSnapshot(String symbol, String interval, BinanceKlineVo kline) {
    }
}
