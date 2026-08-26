package coin.exchange.api.market.service;

import coin.exchange.api.market.factory.RemoteMarketFallbackFactory;
import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "exchange-business-market",
        fallbackFactory = RemoteMarketFallbackFactory.class
)
public interface RemoteMarketService {

    @GetMapping("/market/symbols")
    R<List<MarketSymbolVo>> listSymbols();

    @GetMapping("/market/ticker")
    R<BinanceTickerVo> getBinanceTicker(@RequestParam("symbol") String symbol);

    @GetMapping("/market/depth")
    R<BinanceDepthVo> getBinanceDepth(@RequestParam("symbol") String symbol,
                                      @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/market/trades")
    R<List<BinanceTradeVo>> listBinanceTrades(@RequestParam("symbol") String symbol,
                                              @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/market/klines")
    R<List<BinanceKlineVo>> listBinanceKlines(@RequestParam("symbol") String symbol,
                                              @RequestParam("interval") String interval,
                                              @RequestParam(value = "startTime", required = false) Long startTime,
                                              @RequestParam(value = "endTime", required = false) Long endTime,
                                              @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/market/cache")
    R<MarketCacheSnapshotVo> getBinanceCache(@RequestParam("symbol") String symbol,
                                             @RequestParam(value = "interval", required = false) String interval,
                                             @RequestParam(value = "types", required = false) List<String> types);
}
