package coin.exchange.business.market.service.impl;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.service.RemoteBinanceDataSourceService;
import coin.exchange.business.market.service.BinanceMarketService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BinanceMarketServiceImpl implements BinanceMarketService {

    private final RemoteBinanceDataSourceService remoteBinanceDataSourceService;

    @Override
    public R<BinanceTickerVo> getTicker(String symbol) {
        return remoteBinanceDataSourceService.getTicker(symbol);
    }

    @Override
    public R<BinanceDepthVo> getDepth(String symbol, Integer limit) {
        return remoteBinanceDataSourceService.getDepth(symbol, limit);
    }

    @Override
    public R<List<BinanceTradeVo>> listTrades(String symbol, Integer limit) {
        return remoteBinanceDataSourceService.listTrades(symbol, limit);
    }

    @Override
    public R<List<BinanceKlineVo>> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        return remoteBinanceDataSourceService.listKlines(symbol, interval, startTime, endTime, limit);
    }
}
