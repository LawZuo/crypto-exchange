package coin.exchange.module.datasrouce.service;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;

import java.util.List;

public interface BinanceMarketDataService {

    BinanceTickerVo getTicker(String symbol);

    BinanceDepthVo getDepth(String symbol, Integer limit);

    List<BinanceTradeVo> listTrades(String symbol, Integer limit);

    List<BinanceKlineVo> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit);
}
