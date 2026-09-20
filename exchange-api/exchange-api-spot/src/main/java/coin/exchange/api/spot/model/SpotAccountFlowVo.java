package coin.exchange.api.spot.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpotAccountFlowVo {
    private Long id;
    private Long userId;
    private String asset;
    private BigDecimal changeAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Integer bizType;
    private String bizId;
    private LocalDateTime createTime;
}
