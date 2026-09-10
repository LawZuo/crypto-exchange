package coin.exchange.spot.controller;

import coin.exchange.common.core.response.R;
import coin.exchange.spot.domain.SpotAccountFlowDo;
import coin.exchange.spot.domain.SpotOrderDo;
import coin.exchange.spot.domain.SpotSymbolDo;
import coin.exchange.spot.domain.SpotTradeDo;
import coin.exchange.spot.service.SpotAccountFlowService;
import coin.exchange.spot.service.SpotOrderService;
import coin.exchange.spot.service.SpotSymbolService;
import coin.exchange.spot.service.SpotTradeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
public class SpotQueryController {
    private final SpotOrderService orderService;
    private final SpotTradeService tradeService;
    private final SpotSymbolService symbolService;
    private final SpotAccountFlowService accountFlowService;

    @GetMapping("/orders")
    public R<List<SpotOrderDo>> orders(@RequestParam("userId") Long userId) {
        return R.success(orderService.list(Wrappers.<SpotOrderDo>lambdaQuery()
                .eq(SpotOrderDo::getUserId, userId).orderByDesc(SpotOrderDo::getCreateTime)));
    }

    @GetMapping("/trades")
    public R<List<SpotTradeDo>> trades(@RequestParam("userId") Long userId) {
        return R.success(tradeService.list(Wrappers.<SpotTradeDo>lambdaQuery()
                .eq(SpotTradeDo::getUserId, userId).orderByDesc(SpotTradeDo::getTradeTime)));
    }

    @GetMapping("/symbols")
    public R<List<SpotSymbolDo>> symbols() {
        return R.success(symbolService.list(Wrappers.<SpotSymbolDo>lambdaQuery()
                .eq(SpotSymbolDo::getStatus, 1).orderByAsc(SpotSymbolDo::getSort)));
    }

    @GetMapping("/account-flows")
    public R<List<SpotAccountFlowDo>> accountFlows(@RequestParam("userId") Long userId) {
        return R.success(accountFlowService.list(Wrappers.<SpotAccountFlowDo>lambdaQuery()
                .eq(SpotAccountFlowDo::getUserId, userId).orderByDesc(SpotAccountFlowDo::getCreateTime)));
    }
}
