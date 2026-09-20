package coin.exchange.api.spot.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpotOrderVo {
    private String orderId;
    private Long userId;
    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private Integer side;
    private Integer orderType;
    private BigDecimal orderPrice;
    private BigDecimal orderQuantity;
    private BigDecimal filledQuantity;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
