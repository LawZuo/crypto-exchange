package coin.exchange.business.market.controller;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.business.market.domain.MarketSymbolDo;
import coin.exchange.business.market.service.BinanceMarketService;
import coin.exchange.business.market.service.MarketCacheService;
import coin.exchange.business.market.service.MarketSymbolService;
import coin.exchange.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 市场行情控制层
 */
@RequestMapping("/market")
@RestController
@RequiredArgsConstructor
public class MarketController {

    private final MarketSymbolService marketSymbolService;
    private final BinanceMarketService binanceMarketService;
    private final MarketCacheService marketCacheService;

    /**
     * 交易对列表
     */
    @GetMapping("/symbols")
    public R<List<MarketSymbolVo>> listSymbols(@RequestParam(value = "status", required = false) Integer status) {
        List<MarketSymbolDo> symbols = marketSymbolService.listSymbols(status);
        return R.success(BeanUtil.copyToList(symbols, MarketSymbolVo.class));
    }

    /**
     * 交易对详情
     */
    @GetMapping("/symbols/{symbol}")
    public R<MarketSymbolVo> getSymbol(@PathVariable("symbol") String symbol) {
        MarketSymbolDo result = marketSymbolService.getSymbol(symbol);
        return R.success(BeanUtil.copyProperties(result, MarketSymbolVo.class));
    }

    /**
     * 获取24小时Ticker
     */
    @GetMapping("/ticker")
    public R<BinanceTickerVo> getBinanceTicker(@RequestParam("symbol") String symbol) {
        return binanceMarketService.getTicker(symbol);
    }

    /**
     * 获取深度快照
     */
    @GetMapping("/depth")
    public R<BinanceDepthVo> getBinanceDepth(@RequestParam("symbol") String symbol,
                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return binanceMarketService.getDepth(symbol, limit);
    }

    /**
     * 获取最近成交
     */
    @GetMapping("/trades")
    public R<List<BinanceTradeVo>> listBinanceTrades(@RequestParam("symbol") String symbol,
                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        return binanceMarketService.listTrades(symbol, limit);
    }

    /**
     * 获取历史K线
     */
    @GetMapping("/klines")
    public R<List<BinanceKlineVo>> listBinanceKlines(@RequestParam("symbol") String symbol,
                                                     @RequestParam("interval") String interval,
                                                     @RequestParam(value = "startTime", required = false) Long startTime,
                                                     @RequestParam(value = "endTime", required = false) Long endTime,
                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        return binanceMarketService.listKlines(symbol, interval, startTime, endTime, limit);
    }

    /**
     * 最新缓存快照
     */
    @GetMapping("/cache")
    public R<MarketCacheSnapshotVo> getBinanceCache(@RequestParam("symbol") String symbol,
                                                   @RequestParam(value = "interval", defaultValue = "1m") String interval,
                                                   @RequestParam(value = "types", required = false) List<String> types) {
        return R.success(marketCacheService.getSnapshot(symbol, interval, types));
    }
}
