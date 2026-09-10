package coin.exchange.spot.controller;

import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.common.core.response.R;
import coin.exchange.spot.service.SpotOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/spot")
@RestController
@RequiredArgsConstructor
public class SpotController {

    private final SpotOrderService spotOrderService;

    /**
     * 创建用户下单
     *
     * 这里不做校验，在web层做校验
     */
    @PostMapping("/create")
    public R<String> create(
        @Valid @RequestBody CreateSpotDto spotDto
    ) {
        try {
            // 越界判断
            if (spotDto.getUserId() == null) {
                return R.fail("用户ID不能为空");
            }
            if (spotDto.getQuantity() == null) {
                return R.fail("下单数量不能为空");
            }
            if (!StringUtils.hasText(spotDto.getSymbol())) {
                return R.fail("下单币种不能为空");
            }
            String orderId = spotOrderService.create(spotDto);

            return R.success(orderId);
        } catch (Exception e) {
            log.error("创建现货订单失败: " + e);
            return R.fail("创建现货订单失败");
        }
    }

}
