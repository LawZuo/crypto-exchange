package coin.exchange.business.market.service;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.common.core.response.R;

import java.util.List;

public interface BinanceMarketService {

    R<BinanceTickerVo> getTicker(String symbol);

    R<BinanceDepthVo> getDepth(String symbol, Integer limit);

    R<List<BinanceTradeVo>> listTrades(String symbol, Integer limit);

    R<List<BinanceKlineVo>> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit);
}
