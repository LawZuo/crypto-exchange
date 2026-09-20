package coin.exchange.api.spot.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpotSymbolVo {
    private Long id;
    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private Integer pricePrecision;
    private Integer quantityPrecision;
    private BigDecimal minOrderQuantity;
    private BigDecimal minOrderAmount;
    private Integer status;
    private Integer sort;
}
