package coin.exchange.spot.service;

import coin.exchange.spot.domain.SpotTradeDo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SpotTradeService extends IService<SpotTradeDo> {
    String nextTradeId();
}
