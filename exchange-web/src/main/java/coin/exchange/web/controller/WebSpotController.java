package coin.exchange.web.controller;

import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.api.spot.model.SpotAccountFlowVo;
import coin.exchange.api.spot.model.SpotOrderVo;
import coin.exchange.api.spot.model.SpotSymbolVo;
import coin.exchange.api.spot.model.SpotTradeVo;
import coin.exchange.api.spot.service.RemoteSpotService;
import coin.exchange.common.core.context.SecurityContextHolder;
import coin.exchange.common.core.enums.StatusCode;
import coin.exchange.common.core.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
public class WebSpotController {

    private final RemoteSpotService remoteSpotService;

    @PostMapping("/orders")
    public R<String> createOrder(@Valid @RequestBody CreateSpotDto spotDto) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            return R.fail(StatusCode.UNAUTHORIZED);
        }
        spotDto.setUserId(userId);
        return remoteSpotService.createOrder(spotDto);
    }

    @GetMapping("/orders")
    public R<List<SpotOrderVo>> listOrders() {
        Long userId = SecurityContextHolder.getUserId();
        return userId == null
                ? R.fail(StatusCode.UNAUTHORIZED)
                : remoteSpotService.listOrders(userId);
    }

    @GetMapping("/trades")
    public R<List<SpotTradeVo>> listTrades() {
        Long userId = SecurityContextHolder.getUserId();
        return userId == null
                ? R.fail(StatusCode.UNAUTHORIZED)
                : remoteSpotService.listTrades(userId);
    }

    @GetMapping("/symbols")
    public R<List<SpotSymbolVo>> listSymbols() {
        return remoteSpotService.listSymbols();
    }

    @GetMapping("/account-flows")
    public R<List<SpotAccountFlowVo>> listAccountFlows() {
        Long userId = SecurityContextHolder.getUserId();
        return userId == null
                ? R.fail(StatusCode.UNAUTHORIZED)
                : remoteSpotService.listAccountFlows(userId);
    }
}
