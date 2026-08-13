package coin.exchange.module.datasrouce.controller;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.common.core.response.R;
import coin.exchange.module.datasrouce.service.BinanceMarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/datasource/binance")
@RestController
@RequiredArgsConstructor
public class BinanceDataSourceController {

    private final BinanceMarketDataService binanceMarketDataService;

    @GetMapping("/ticker")
    public R<BinanceTickerVo> getTicker(@RequestParam("symbol") String symbol) {
        return R.success(binanceMarketDataService.getTicker(symbol));
    }

    @GetMapping("/depth")
    public R<BinanceDepthVo> getDepth(@RequestParam("symbol") String symbol,
                                      @RequestParam(value = "limit", required = false) Integer limit) {
        return R.success(binanceMarketDataService.getDepth(symbol, limit));
    }

    @GetMapping("/trades")
    public R<List<BinanceTradeVo>> listTrades(@RequestParam("symbol") String symbol,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
        return R.success(binanceMarketDataService.listTrades(symbol, limit));
    }

    @GetMapping("/klines")
    public R<List<BinanceKlineVo>> listKlines(@RequestParam("symbol") String symbol,
                                              @RequestParam("interval") String interval,
                                              @RequestParam(value = "startTime", required = false) Long startTime,
                                              @RequestParam(value = "endTime", required = false) Long endTime,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
        return R.success(binanceMarketDataService.listKlines(symbol, interval, startTime, endTime, limit));
    }
}
