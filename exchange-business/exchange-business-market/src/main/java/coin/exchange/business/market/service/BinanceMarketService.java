package coin.exchange.business.market.service;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.common.core.response.R;

import java.util.List;

/**
 * 币安数据源服务 - 从内存中获取
 */
public interface BinanceMarketService {

    /**
     * 获取24小时Ticker
     */
    R<BinanceTickerVo> getTicker(String symbol);

    /**
     * 获取深度快照
     */
    R<BinanceDepthVo> getDepth(String symbol, Integer limit);

    /**
     * 获取最近成交
     */
    R<List<BinanceTradeVo>> listTrades(String symbol, Integer limit);

    /**
     * 获取历史K线
     */
    R<List<BinanceKlineVo>> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit);
}
