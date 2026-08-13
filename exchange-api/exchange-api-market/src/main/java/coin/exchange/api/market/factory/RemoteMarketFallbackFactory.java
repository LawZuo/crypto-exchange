package coin.exchange.api.market.factory;

import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.service.RemoteMarketService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RemoteMarketFallbackFactory implements FallbackFactory<RemoteMarketService> {

    @Override
    public RemoteMarketService create(Throwable throwable) {
        log.error("行情服务调用失败: {}", throwable.getMessage());
        return new RemoteMarketService() {
            @Override
            public R<List<MarketSymbolVo>> listSymbols() {
                return R.fail("Feign调取交易对列表失败" + throwable.getMessage());
            }

            @Override
            public R<BinanceTickerVo> getBinanceTicker(String symbol) {
                return R.fail("Feign调取Binance ticker失败" + throwable.getMessage());
            }

            @Override
            public R<BinanceDepthVo> getBinanceDepth(String symbol, Integer limit) {
                return R.fail("Feign调取Binance depth失败" + throwable.getMessage());
            }

            @Override
            public R<List<BinanceTradeVo>> listBinanceTrades(String symbol, Integer limit) {
                return R.fail("Feign调取Binance trades失败" + throwable.getMessage());
            }

            @Override
            public R<List<BinanceKlineVo>> listBinanceKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
                return R.fail("Feign调取Binance klines失败" + throwable.getMessage());
            }

            @Override
            public R<MarketCacheSnapshotVo> getBinanceCache(String symbol, String interval, List<String> types) {
                return R.fail("Feign调取Binance缓存快照失败" + throwable.getMessage());
            }
        };
    }
}
