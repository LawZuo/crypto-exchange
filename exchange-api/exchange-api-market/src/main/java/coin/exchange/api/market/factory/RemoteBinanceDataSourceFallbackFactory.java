package coin.exchange.api.market.factory;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.service.RemoteBinanceDataSourceService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RemoteBinanceDataSourceFallbackFactory implements FallbackFactory<RemoteBinanceDataSourceService> {

    @Override
    public RemoteBinanceDataSourceService create(Throwable throwable) {
        log.error("Binance数据源调用失败: {}", throwable.getMessage());
        return new RemoteBinanceDataSourceService() {
            @Override
            public R<BinanceTickerVo> getTicker(String symbol) {
                return R.fail("Feign调取Binance ticker失败" + throwable.getMessage());
            }

            @Override
            public R<BinanceDepthVo> getDepth(String symbol, Integer limit) {
                return R.fail("Feign调取Binance depth失败" + throwable.getMessage());
            }

            @Override
            public R<List<BinanceTradeVo>> listTrades(String symbol, Integer limit) {
                return R.fail("Feign调取Binance trades失败" + throwable.getMessage());
            }

            @Override
            public R<List<BinanceKlineVo>> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
                return R.fail("Feign调取Binance klines失败" + throwable.getMessage());
            }
        };
    }
}
