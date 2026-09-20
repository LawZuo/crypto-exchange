package coin.exchange.api.market.model;

import lombok.Data;

import java.math.BigDecimal;

/** 行情榜单条目。 */
@Data
public class MarketRankVo {
    private String symbol;
    private String baseCurrency;
    private String quoteCurrency;
    private BigDecimal lastPrice;
    private BigDecimal priceChangePercent;
    private BigDecimal volume;
    private BigDecimal quoteVolume;
    private Long updateTime;
}
