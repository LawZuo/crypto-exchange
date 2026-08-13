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

    private Integer pricePrecision;

    private Integer quantityPrecision;

    private BigDecimal minOrderQuantity;

    private BigDecimal minOrderAmount;

    private Integer status;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
