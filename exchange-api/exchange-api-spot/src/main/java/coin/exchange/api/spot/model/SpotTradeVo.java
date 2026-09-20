package coin.exchange.api.spot.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpotTradeVo {
    private String tradeId;
    private String orderId;
    private Long userId;
    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private Integer side;
    private BigDecimal tradePrice;
    private BigDecimal tradeQuantity;
    private BigDecimal tradeAmount;
    private BigDecimal fee;
    private String feeAsset;
    private BigDecimal feeUsdtValue;
    private BigDecimal marketPrice;
    private LocalDateTime tradeTime;
    private LocalDateTime createTime;
}
