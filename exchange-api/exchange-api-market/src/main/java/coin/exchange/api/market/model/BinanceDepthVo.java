package coin.exchange.api.market.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BinanceDepthVo {

    // 毫秒时间戳
    private Long lastUpdateId;

    // 盘口
    private List<List<BigDecimal>> bids;

    // 盘口
    private List<List<BigDecimal>> asks;
}
