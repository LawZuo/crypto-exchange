package coin.exchange.api.spot.service;

import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.api.spot.factory.RemoteSpotFallbackFactory;
import coin.exchange.api.spot.model.SpotAccountFlowVo;
import coin.exchange.api.spot.model.SpotOrderVo;
import coin.exchange.api.spot.model.SpotSymbolVo;
import coin.exchange.api.spot.model.SpotTradeVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "exchange-business-spot", fallbackFactory = RemoteSpotFallbackFactory.class)
public interface RemoteSpotService {

    @PostMapping("/spot/create")
    R<String> createOrder(@RequestBody CreateSpotDto spotDto);

    @GetMapping("/spot/orders")
    R<List<SpotOrderVo>> listOrders(@RequestParam("userId") Long userId);

    @GetMapping("/spot/trades")
    R<List<SpotTradeVo>> listTrades(@RequestParam("userId") Long userId);

    @GetMapping("/spot/symbols")
    R<List<SpotSymbolVo>> listSymbols();

    @GetMapping("/spot/account-flows")
    R<List<SpotAccountFlowVo>> listAccountFlows(@RequestParam("userId") Long userId);
}
