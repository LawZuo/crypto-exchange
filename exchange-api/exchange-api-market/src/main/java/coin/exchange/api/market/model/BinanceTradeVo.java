package coin.exchange.api.market.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinanceTradeVo {

    private Long id;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal quoteQuantity;
    private Long time;
    private Boolean buyerMaker;
    private Boolean bestMatch;
}
