package coin.exchange.api.spot.factory;

import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.api.spot.model.SpotAccountFlowVo;
import coin.exchange.api.spot.model.SpotOrderVo;
import coin.exchange.api.spot.model.SpotSymbolVo;
import coin.exchange.api.spot.model.SpotTradeVo;
import coin.exchange.api.spot.service.RemoteSpotService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

@Slf4j
public class RemoteSpotFallbackFactory implements FallbackFactory<RemoteSpotService> {
    @Override
    public RemoteSpotService create(Throwable throwable) {
        log.error("现货服务调用失败: {}", throwable.getMessage());
        return new RemoteSpotService() {
            @Override
            public R<String> createOrder(CreateSpotDto spotDto) {
                return R.fail("创建现货订单失败: " + throwable.getMessage());
            }

            @Override
            public R<List<SpotOrderVo>> listOrders(Long userId) {
                return R.fail("查询现货订单失败: " + throwable.getMessage());
            }

            @Override
            public R<List<SpotTradeVo>> listTrades(Long userId) {
                return R.fail("查询现货成交记录失败: " + throwable.getMessage());
            }

            @Override
            public R<List<SpotSymbolVo>> listSymbols() {
                return R.fail("查询现货交易对失败: " + throwable.getMessage());
            }

            @Override
            public R<List<SpotAccountFlowVo>> listAccountFlows(Long userId) {
                return R.fail("查询现货账户流水失败: " + throwable.getMessage());
            }
        };
    }
}
