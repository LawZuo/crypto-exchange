package coin.exchange.module.datasrouce.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 行情快照
 * 使用redis兜底备份
 * 作用：每秒把 datasource 内存里的最新行情写一份到 Redis
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRedisSnapshotService {

    private final MarketMemoryCache marketMemoryCache;
    private final KlineCacheService klineCacheService;

    @Scheduled(fixedRateString = "${exchange.market.binance.snapshot-interval-ms:1000}")
    public void snapshotToRedis() {
        marketMemoryCache.tickerSnapshot()
                .forEach((symbol, ticker) -> klineCacheService.cacheTickerData(symbol, ticker));
        marketMemoryCache.depthSnapshot()
                .forEach((symbol, depth) -> klineCacheService.cacheDepthData(symbol, depth));
        marketMemoryCache.tradeSnapshot()
                .forEach((symbol, trade) -> klineCacheService.cacheTradeData(symbol, trade));
        marketMemoryCache.klineSnapshot()
                .forEach(snapshot -> klineCacheService.cacheKlineData(snapshot.symbol(), snapshot.interval(), snapshot.kline()));
    }
}
