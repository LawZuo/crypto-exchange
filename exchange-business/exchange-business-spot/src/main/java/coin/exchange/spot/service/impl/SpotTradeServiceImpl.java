package coin.exchange.spot.service.impl;

import coin.exchange.spot.domain.SpotTradeDo;
import coin.exchange.spot.mapper.SpotTradeMapper;
import coin.exchange.spot.service.SpotTradeService;
import coin.exchange.spot.util.SpotIdGenerator;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SpotTradeServiceImpl extends ServiceImpl<SpotTradeMapper, SpotTradeDo> implements SpotTradeService {
    @Override
    public String nextTradeId() {
        return SpotIdGenerator.tradeId();
    }
}
