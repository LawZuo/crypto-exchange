package coin.exchange.business.market.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行情交易对
 */
@Data
@TableName("market_symbol")
public class MarketSymbolDo {

    // 主键ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 交易对
    @TableField("symbol")
    private String symbol;

    // 基础币种
    @TableField("base_currency")
    private String baseCurrency;

    // 报价币种
    @TableField("quote_currency")
    private String quoteCurrency;

    // 价格精度
    @TableField("price_precision")
    private Integer pricePrecision;

    // 数量精度
    @TableField("quantity_precision")
    private Integer quantityPrecision;

    // 最小下单数量
    @TableField("min_order_quantity")
    private BigDecimal minOrderQuantity;

    // 最小下单金额
    @TableField("min_order_amount")
    private BigDecimal minOrderAmount;

    // 状态
    @TableField("status")
    private Integer status;

    // 排序
    @TableField("sort")
    private Integer sort;

    // 备注
    @TableField("remark")
    private String remark;

    // 创建时间
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    // 修改时间
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
