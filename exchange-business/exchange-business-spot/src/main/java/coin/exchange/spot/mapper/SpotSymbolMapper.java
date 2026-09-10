package coin.exchange.spot.mapper;

import coin.exchange.spot.domain.SpotSymbolDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpotSymbolMapper extends BaseMapper<SpotSymbolDo> {
}
