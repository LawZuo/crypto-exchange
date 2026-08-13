package coin.exchange.module.datasrouce.cache;

import coin.exchange.common.redis.service.RedisService;
import coin.exchange.module.datasrouce.domain.DepthWsMessageDo;
import coin.exchange.module.datasrouce.domain.KlineWsMessageDo;
import coin.exchange.module.datasrouce.domain.TickerWsMessageDo;
import coin.exchange.module.datasrouce.domain.TradeWsMessageDo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KlineCacheService {

    private final RedisService redisService;

    private static final String KLINE_KEY_PREFIX = "kline:latest:";
    private static final String MARKET_KEY_PREFIX = "market:";
    private static final long TTL_SECONDS = 180;

    /**
     * 缓存k线数据
     */
    public void cacheKlineData(String symbol, String interval, KlineWsMessageDo.Source.Kline kline) {
        String key = KLINE_KEY_PREFIX + symbol.toLowerCase() + ":" + interval;
        redisService.setCacheObject(key, kline, TTL_SECONDS, TimeUnit.SECONDS);
        log.info("更新最新K线缓存: key={}, ttl={}s, value={}", key, TTL_SECONDS, kline);
    }

    /**
     * 缓存24小时ticker数据
     */
    public void cacheTickerData(String symbol, TickerWsMessageDo.Source ticker) {
        String key = marketKey("ticker", symbol);
        redisService.setCacheObject(key, ticker, TTL_SECONDS, TimeUnit.SECONDS);
        log.info("更新Ticker缓存: key={}, ttl={}s, value={}", key, TTL_SECONDS, ticker);
    }

    /**
     * 缓存深度增量数据
     */
    public void cacheDepthData(String symbol, DepthWsMessageDo.Source depth) {
        String key = marketKey("depth", symbol);
        redisService.setCacheObject(key, depth, TTL_SECONDS, TimeUnit.SECONDS);
        log.info("更新Depth缓存: key={}, ttl={}s, value={}", key, TTL_SECONDS, depth);
    }

    /**
     * 缓存最近成交数据
     */
    public void cacheTradeData(String symbol, TradeWsMessageDo.Source trade) {
        String key = marketKey("trade", symbol);
        redisService.setCacheObject(key, trade, TTL_SECONDS, TimeUnit.SECONDS);
        log.info("更新Trade缓存: key={}, ttl={}s, value={}", key, TTL_SECONDS, trade);
    }

    private String marketKey(String type, String symbol) {
        return MARKET_KEY_PREFIX + type + ":" + symbol.toLowerCase();
    }
}
