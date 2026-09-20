package coin.exchange.business.market.service.impl;

import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.business.market.domain.MarketSymbolDo;
import coin.exchange.business.market.mapper.MarketSymbolMapper;
import coin.exchange.business.market.service.MarketSymbolService;
import coin.exchange.common.core.constant.RedisKeyConstants;
import coin.exchange.common.redis.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 交易对服务实现类
 */
@RequiredArgsConstructor
@Service
public class MarketSymbolServiceImpl implements MarketSymbolService {

    private final MarketSymbolMapper marketSymbolMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public MarketSymbolDo getSymbol(String symbol) {
        if (Objects.isNull(symbol) || symbol.isBlank()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
        MarketSymbolDo marketSymbol = marketSymbolMapper.getBySymbol(normalizeSymbol(symbol));
        fillTickerData(marketSymbol);
        return marketSymbol;
    }

    @Override
    public List<MarketSymbolDo> listSymbols(Integer status) {
        // 1. 从数据库获取所有交易对
        List<MarketSymbolDo> marketList = status == null
                ? marketSymbolMapper.listAll()
                : marketSymbolMapper.listByStatus(status);

        // 2. 从 Redis 最新 ticker 缓存补充市场价格和24小时涨跌数据
        marketList.forEach(this::fillTickerData);
        return marketList;
    }

    private void fillTickerData(MarketSymbolDo marketSymbol) {
        if (marketSymbol == null || marketSymbol.getSymbol() == null) {
            return;
        }
        String tickerKey = RedisKeyConstants.TICKER_KEY_PREFIX
                + marketSymbol.getSymbol().trim().toLowerCase(Locale.ROOT);
        Object cachedTicker = redisService.getCacheObject(tickerKey);
        if (cachedTicker == null) {
            return;
        }
        BinanceTickerVo ticker = objectMapper.convertValue(cachedTicker, BinanceTickerVo.class);
        marketSymbol.setLastPrice(ticker.getLastPrice());
        marketSymbol.setPriceChange(ticker.getPriceChange());
        marketSymbol.setPriceChangePercent(ticker.getPriceChangePercent());
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
}
