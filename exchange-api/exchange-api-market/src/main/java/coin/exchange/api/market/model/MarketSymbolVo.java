package coin.exchange.api.market.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易对信息
 */
@Data
public class MarketSymbolVo {

    private Long id;

    private String symbol;

    private String baseCurrency;

    private String quoteCurrency;

    private Integer status;

    private Integer sort;

    private String remark;

    private BigDecimal lastPrice;

    private BigDecimal priceChange;

    private BigDecimal priceChangePercent;
}
