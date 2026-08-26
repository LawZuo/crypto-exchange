package coin.exchange.web.controller;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.api.market.service.RemoteMarketService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class WebMarketController {

    private final RemoteMarketService remoteMarketService;

    @GetMapping("/symbols")
    public R<List<MarketSymbolVo>> listSymbols() {
        return remoteMarketService.listSymbols();
    }

    @GetMapping("/ticker")
    public R<BinanceTickerVo> getTicker(@RequestParam("symbol") String symbol) {
        return remoteMarketService.getBinanceTicker(symbol);
    }

    @GetMapping("/depth")
    public R<BinanceDepthVo> getDepth(@RequestParam("symbol") String symbol,
                                     @RequestParam(value = "limit", required = false) Integer limit) {
        return remoteMarketService.getBinanceDepth(symbol, limit);
    }

    @GetMapping("/trades")
    public R<List<BinanceTradeVo>> listTrades(@RequestParam("symbol") String symbol,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
        return remoteMarketService.listBinanceTrades(symbol, limit);
    }

    @GetMapping("/klines")
    public R<List<BinanceKlineVo>> listKlines(@RequestParam("symbol") String symbol,
                                              @RequestParam("interval") String interval,
                                              @RequestParam(value = "startTime", required = false) Long startTime,
                                              @RequestParam(value = "endTime", required = false) Long endTime,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
        return remoteMarketService.listBinanceKlines(symbol, interval, startTime, endTime, limit);
    }

    @GetMapping("/cache")
    public R<MarketCacheSnapshotVo> getCache(@RequestParam("symbol") String symbol,
                                             @RequestParam(value = "interval", required = false) String interval,
                                             @RequestParam(value = "types", required = false) List<String> types) {
        return remoteMarketService.getBinanceCache(symbol, interval, types);
    }
}
