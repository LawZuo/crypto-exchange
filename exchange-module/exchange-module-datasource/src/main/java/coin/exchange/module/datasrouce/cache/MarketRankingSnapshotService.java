package coin.exchange.module.datasrouce.cache;

import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.api.market.service.RemoteMarketService;
import coin.exchange.common.core.response.R;
import coin.exchange.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** 将 Binance 24h ticker 投影为 Redis 实时榜单。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRankingSnapshotService {

    public static final String CHANGE_RANK_KEY = "market:rank:change:24h";
    public static final String VOLUME_RANK_KEY = "market:rank:quote-volume:24h";
    public static final String UPDATED_AT_KEY = "market:rank:updated-at";
    private static final long TTL_SECONDS = 180;

    private final MarketMemoryCache marketMemoryCache;
    private final RemoteMarketService remoteMarketService;
    private final RedisService redisService;
    private volatile Set<String> enabledSymbols = Set.of();

    @Scheduled(initialDelay = 0, fixedDelayString = "${exchange.market.ranking.symbol-refresh-ms:30000}")
    public void refreshEnabledSymbols() {
        try {
            R<java.util.List<MarketSymbolVo>> response = remoteMarketService.listSymbols(1);
            if (response == null || response.code() != R.SUCCESS_CODE || response.data() == null) {
                log.warn("刷新行情榜单交易对失败: {}", response == null ? "无响应" : response.message());
                return;
            }
            Set<String> symbols = new HashSet<>();
            for (MarketSymbolVo symbol : response.data()) {
                if (symbol.getSymbol() != null && !symbol.getSymbol().isBlank()) {
                    symbols.add(symbol.getSymbol().trim().toUpperCase(Locale.ROOT));
                }
            }
            enabledSymbols = Set.copyOf(symbols);
        } catch (Exception e) {
            log.warn("刷新行情榜单交易对异常: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRateString = "${exchange.market.ranking.refresh-ms:1000}")
    public void refreshRankings() {
        Set<String> symbols = enabledSymbols;
        if (symbols.isEmpty()) {
            return;
        }

        Map<String, BinanceTickerVo> tickers = marketMemoryCache.tickerSnapshot();
        for (String symbol : symbols) {
            BinanceTickerVo ticker = tickers.get(symbol.toLowerCase(Locale.ROOT));
            if (ticker == null) {
                continue;
            }
            redisService.addToSortedSet(CHANGE_RANK_KEY, symbol, score(ticker.getPriceChangePercent()));
            redisService.addToSortedSet(VOLUME_RANK_KEY, symbol, score(ticker.getQuoteVolume()));
        }

        redisService.removeSortedSetMembersNotIn(CHANGE_RANK_KEY, symbols);
        redisService.removeSortedSetMembersNotIn(VOLUME_RANK_KEY, symbols);
        redisService.expire(CHANGE_RANK_KEY, TTL_SECONDS, TimeUnit.SECONDS);
        redisService.expire(VOLUME_RANK_KEY, TTL_SECONDS, TimeUnit.SECONDS);
        redisService.setCacheObject(UPDATED_AT_KEY, System.currentTimeMillis(), TTL_SECONDS, TimeUnit.SECONDS);
    }

    private double score(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }
}
