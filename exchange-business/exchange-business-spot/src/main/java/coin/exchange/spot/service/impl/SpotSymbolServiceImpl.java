package coin.exchange.spot.service.impl;

import coin.exchange.spot.domain.SpotSymbolDo;
import coin.exchange.spot.mapper.SpotSymbolMapper;
import coin.exchange.spot.service.SpotSymbolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SpotSymbolServiceImpl extends ServiceImpl<SpotSymbolMapper, SpotSymbolDo> implements SpotSymbolService {
}
