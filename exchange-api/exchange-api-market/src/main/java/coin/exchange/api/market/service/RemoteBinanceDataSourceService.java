package coin.exchange.api.market.service;

import coin.exchange.api.market.factory.RemoteBinanceDataSourceFallbackFactory;
import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "exchange-service-datasource",
        url = "${bt.datasource.base-url:http://localhost:8077}",
        fallbackFactory = RemoteBinanceDataSourceFallbackFactory.class
)
public interface RemoteBinanceDataSourceService {

    @GetMapping("/datasource/binance/ticker")
    R<BinanceTickerVo> getTicker(@RequestParam("symbol") String symbol);

    @GetMapping("/datasource/binance/depth")
    R<BinanceDepthVo> getDepth(@RequestParam("symbol") String symbol,
                               @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/datasource/binance/trades")
    R<List<BinanceTradeVo>> listTrades(@RequestParam("symbol") String symbol,
                                       @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/datasource/binance/klines")
    R<List<BinanceKlineVo>> listKlines(@RequestParam("symbol") String symbol,
                                       @RequestParam("interval") String interval,
                                       @RequestParam(value = "startTime", required = false) Long startTime,
                                       @RequestParam(value = "endTime", required = false) Long endTime,
                                       @RequestParam(value = "limit", required = false) Integer limit);
}
