package coin.exchange.spot.service.impl;

import coin.exchange.spot.domain.SpotAccountFlowDo;
import coin.exchange.spot.mapper.SpotAccountFlowMapper;
import coin.exchange.spot.service.SpotAccountFlowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SpotAccountFlowServiceImpl extends ServiceImpl<SpotAccountFlowMapper, SpotAccountFlowDo> implements SpotAccountFlowService {
}
