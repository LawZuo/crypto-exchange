package coin.exchange.spot.service;

import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.spot.domain.SpotOrderDo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SpotOrderService extends IService<SpotOrderDo> {

    // 创建订单
    String create(CreateSpotDto spotDto);
}
