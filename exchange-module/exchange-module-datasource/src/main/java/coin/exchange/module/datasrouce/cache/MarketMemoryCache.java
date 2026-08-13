package coin.exchange.module.datasrouce.cache;

import coin.exchange.module.datasrouce.domain.DepthWsMessageDo;
import coin.exchange.module.datasrouce.domain.KlineWsMessageDo;
import coin.exchange.module.datasrouce.domain.TickerWsMessageDo;
import coin.exchange.module.datasrouce.domain.TradeWsMessageDo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketMemoryCache {

    private final ConcurrentHashMap<String, TickerWsMessageDo.Source> tickerCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DepthWsMessageDo.Source> depthCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TradeWsMessageDo.Source> tradeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, KlineWsMessageDo.Source.Kline> klineCache = new ConcurrentHashMap<>();

    public void putTicker(String symbol, TickerWsMessageDo.Source ticker) {
        tickerCache.put(symbolKey(symbol), ticker);
    }

    public void putDepth(String symbol, DepthWsMessageDo.Source depth) {
        depthCache.put(symbolKey(symbol), depth);
    }

    public void putTrade(String symbol, TradeWsMessageDo.Source trade) {
        tradeCache.put(symbolKey(symbol), trade);
    }

    public void putKline(String symbol, String interval, KlineWsMessageDo.Source.Kline kline) {
        klineCache.put(klineKey(symbol, interval), kline);
    }

    public Map<String, TickerWsMessageDo.Source> tickerSnapshot() {
        return Map.copyOf(tickerCache);
    }

    public Map<String, DepthWsMessageDo.Source> depthSnapshot() {
        return Map.copyOf(depthCache);
    }

    public Map<String, TradeWsMessageDo.Source> tradeSnapshot() {
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

    public record KlineSnapshot(String symbol, String interval, KlineWsMessageDo.Source.Kline kline) {
    }
}
