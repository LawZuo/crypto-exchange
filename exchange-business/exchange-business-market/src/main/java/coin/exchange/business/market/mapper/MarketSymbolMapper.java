package coin.exchange.business.market.mapper;

import coin.exchange.business.market.domain.MarketSymbolDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MarketSymbolMapper {

    @Select("select * from market_symbol where is_deleted = 0 order by sort asc, id asc")
    List<MarketSymbolDo> listAll();

    @Select("select * from market_symbol where status = #{status} and is_deleted = 0 order by sort asc, id asc")
    List<MarketSymbolDo> listByStatus(@Param("status") Integer status);

    @Select("select * from market_symbol where symbol = #{symbol} and is_deleted = 0")
    MarketSymbolDo getBySymbol(@Param("symbol") String symbol);
}
