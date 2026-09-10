package coin.exchange.spot.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("spot_trade")
public class SpotTradeDo {
    @TableId(value = "trade_id", type = IdType.INPUT)
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
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
