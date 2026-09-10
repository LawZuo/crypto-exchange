package coin.exchange.spot.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("spot_symbol")
public class SpotSymbolDo {
    @TableId(value = "id", type = IdType.AUTO)
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
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
