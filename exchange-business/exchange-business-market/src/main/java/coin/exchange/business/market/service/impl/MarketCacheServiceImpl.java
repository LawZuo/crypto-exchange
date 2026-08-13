package coin.exchange.business.market.service.impl;

import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketStreamMessageVo;
import coin.exchange.business.market.service.MarketCacheService;
import coin.exchange.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MarketCacheServiceImpl implements MarketCacheService {

    private static final String KLINE_KEY_PREFIX = "kline:latest:";
    private static final String MARKET_KEY_PREFIX = "market:";
    private static final Set<String> DEFAULT_TYPES = Set.of("ticker", "depth", "trade", "kline");

    private final RedisService redisService;
    private final ConcurrentHashMap<String, Object> tickerCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> depthCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> tradeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> klineCache = new ConcurrentHashMap<>();

    @Override
    public Object getTicker(String symbol) {
        String key = normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
        Object value = tickerCache.get(key);
        return value != null ? value : redisService.getCacheObject(marketKey("ticker", symbol));
    }

    @Override
    public Object getDepth(String symbol) {
        String key = normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
        Object value = depthCache.get(key);
        return value != null ? value : redisService.getCacheObject(marketKey("depth", symbol));
    }

    @Override
    public Object getTrade(String symbol) {
        String key = normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
        Object value = tradeCache.get(key);
        return value != null ? value : redisService.getCacheObject(marketKey("trade", symbol));
    }

    @Override
    public Object getKline(String symbol, String interval) {
        String key = klineMemoryKey(symbol, interval);
        Object value = klineCache.get(key);
        return value != null ? value : redisService.getCacheObject(klineKey(symbol, interval));
    }

    @Override
    public MarketCacheSnapshotVo getSnapshot(String symbol, String interval, Collection<String> types) {
        Set<String> normalizedTypes = normalizeTypes(types);
        MarketCacheSnapshotVo snapshot = new MarketCacheSnapshotVo();
        snapshot.setSymbol(normalizeSymbol(symbol));
        snapshot.setInterval(normalizeInterval(interval));
        if (normalizedTypes.contains("ticker")) {
            snapshot.setTicker(getTicker(symbol));
        }
        if (normalizedTypes.contains("depth")) {
            snapshot.setDepth(getDepth(symbol));
        }
        if (normalizedTypes.contains("trade")) {
            snapshot.setTrade(getTrade(symbol));
        }
        if (normalizedTypes.contains("kline")) {
            snapshot.setKline(getKline(symbol, interval));
        }
        return snapshot;
    }

    @Override
    public void update(MarketStreamMessageVo message) {
        if (message == null || message.getType() == null || message.getSymbol() == null) {
            return;
        }
        String symbol = normalizeSymbol(message.getSymbol()).toLowerCase(Locale.ROOT);
        String type = message.getType().trim().toLowerCase(Locale.ROOT);
        Object payload = message.getPayload();
        switch (type) {
            case "ticker" -> tickerCache.put(symbol, payload);
            case "depth" -> depthCache.put(symbol, payload);
            case "trade" -> tradeCache.put(symbol, payload);
            case "kline" -> klineCache.put(klineMemoryKey(symbol, message.getInterval()), payload);
            default -> {
            }
        }
    }

    @Override
    public void warmUp(Collection<String> symbols, String interval) {
        if (symbols == null || symbols.isEmpty()) {
            return;
        }
        symbols.stream()
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .forEach(symbol -> {
                    String normalizedSymbol = normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
                    Object ticker = redisService.getCacheObject(marketKey("ticker", symbol));
                    Object depth = redisService.getCacheObject(marketKey("depth", symbol));
                    Object trade = redisService.getCacheObject(marketKey("trade", symbol));
                    Object kline = redisService.getCacheObject(klineKey(symbol, interval));
                    if (ticker != null) {
                        tickerCache.put(normalizedSymbol, ticker);
                    }
                    if (depth != null) {
                        depthCache.put(normalizedSymbol, depth);
                    }
                    if (trade != null) {
                        tradeCache.put(normalizedSymbol, trade);
                    }
                    if (kline != null) {
                        klineCache.put(klineMemoryKey(symbol, interval), kline);
                    }
                });
    }

    private Set<String> normalizeTypes(Collection<String> types) {
        if (types == null || types.isEmpty()) {
            return DEFAULT_TYPES;
        }
        return types.stream()
                .filter(type -> type != null && !type.isBlank())
                .map(type -> type.trim().toLowerCase(Locale.ROOT))
                .filter(DEFAULT_TYPES::contains)
                .collect(java.util.stream.Collectors.toSet());
    }

    private String marketKey(String type, String symbol) {
        return MARKET_KEY_PREFIX + type + ":" + normalizeSymbol(symbol).toLowerCase(Locale.ROOT);
    }

    private String klineKey(String symbol, String interval) {
        return KLINE_KEY_PREFIX + normalizeSymbol(symbol).toLowerCase(Locale.ROOT) + ":" + normalizeInterval(interval);
    }

    private String klineMemoryKey(String symbol, String interval) {
        return normalizeSymbol(symbol).toLowerCase(Locale.ROOT) + ":" + normalizeInterval(interval);
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeInterval(String interval) {
        if (interval == null || interval.isBlank()) {
            return "1m";
        }
        return interval.trim();
    }
}
