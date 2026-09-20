package coin.exchange.business.market.service.impl;

import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.MarketRankVo;
import coin.exchange.business.market.domain.MarketSymbolDo;
import coin.exchange.business.market.service.MarketRankingService;
import coin.exchange.business.market.service.MarketSymbolService;
import coin.exchange.common.core.constant.RedisKeyConstants;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.exception.BusinessException;
import coin.exchange.common.redis.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class MarketRankingServiceImpl implements MarketRankingService {

    private static final String CHANGE_RANK_KEY = RedisKeyConstants.CHANGE_RANK_KEY;
    private static final String VOLUME_RANK_KEY = RedisKeyConstants.VOLUME_RANK_KEY;
    private static final String TICKER_KEY_PREFIX = RedisKeyConstants.TICKER_KEY_PREFIX;

    private final RedisService redisService;
    private final MarketSymbolService marketSymbolService;
    private final ObjectMapper objectMapper;

    @Override
    public List<MarketRankVo> listGainers(Integer limit) {
        return list(CHANGE_RANK_KEY, limit, true,
                ticker -> ticker.getPriceChangePercent() != null
                        && ticker.getPriceChangePercent().signum() > 0);
    }

    @Override
    public List<MarketRankVo> listLosers(Integer limit) {
        return list(CHANGE_RANK_KEY, limit, false,
                ticker -> ticker.getPriceChangePercent() != null
                        && ticker.getPriceChangePercent().signum() < 0);
    }

    @Override
    public List<MarketRankVo> listVolume(Integer limit) {
        return list(VOLUME_RANK_KEY, limit, true, ticker -> true);
    }

    private List<MarketRankVo> list(String key, Integer requestedLimit, boolean descending,
                                    Predicate<BinanceTickerVo> tickerFilter) {
        int limit = validateLimit(requestedLimit);
        Map<String, MarketSymbolDo> enabledSymbols = enabledSymbolMap();
        if (enabledSymbols.isEmpty()) {
            return List.of();
        }

        Set<Object> rankedSymbols = descending
                ? redisService.reverseRangeSortedSet(key, 0, -1)
                : redisService.rangeSortedSet(key, 0, -1);
        if (rankedSymbols == null || rankedSymbols.isEmpty()) {
            return List.of();
        }

        List<MarketRankVo> result = new ArrayList<>(limit);
        for (Object member : rankedSymbols) {
            String symbol = String.valueOf(member).trim().toUpperCase(Locale.ROOT);
            MarketSymbolDo marketSymbol = enabledSymbols.get(symbol);
            if (marketSymbol == null) {
                continue;
            }
            Object cachedTicker = redisService.getCacheObject(TICKER_KEY_PREFIX + symbol.toLowerCase(Locale.ROOT));
            if (cachedTicker == null) {
                continue;
            }
            BinanceTickerVo ticker = objectMapper.convertValue(cachedTicker, BinanceTickerVo.class);
            if (!tickerFilter.test(ticker)) {
                continue;
            }
            result.add(toRankVo(marketSymbol, ticker));
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private int validateLimit(Integer limit) {
        int value = limit == null ? 10 : limit;
        if (value < 1 || value > 100) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "limit必须在1到100之间");
        }
        return value;
    }

    private Map<String, MarketSymbolDo> enabledSymbolMap() {
        Map<String, MarketSymbolDo> result = new LinkedHashMap<>();
        for (MarketSymbolDo symbol : marketSymbolService.listSymbols(1)) {
            result.put(symbol.getSymbol().trim().toUpperCase(Locale.ROOT), symbol);
        }
        return result;
    }

    private MarketRankVo toRankVo(MarketSymbolDo symbol, BinanceTickerVo ticker) {
        MarketRankVo item = new MarketRankVo();
        item.setSymbol(symbol.getSymbol());
        item.setBaseCurrency(symbol.getBaseCurrency());
        item.setQuoteCurrency(symbol.getQuoteCurrency());
        item.setLastPrice(ticker.getLastPrice());
        item.setPriceChangePercent(ticker.getPriceChangePercent());
        item.setVolume(ticker.getVolume());
        item.setQuoteVolume(ticker.getQuoteVolume());
        item.setUpdateTime(ticker.getCloseTime());
        return item;
    }
}
