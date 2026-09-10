package coin.exchange.spot.mapper;

import coin.exchange.spot.domain.SpotTradeDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpotTradeMapper extends BaseMapper<SpotTradeDo> {
}
