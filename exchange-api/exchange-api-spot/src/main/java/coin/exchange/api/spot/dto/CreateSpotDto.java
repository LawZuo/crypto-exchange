package coin.exchange.api.spot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建现货订单参数
 */
@Data
public class CreateSpotDto {

    // 用户id
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // 币种
    @NotBlank(message = "交易对不能为空")
    private String symbol;

    // 买卖方向
    private Integer side;

    // 类型：市价 & 限价
    private Integer orderType;

    // 委托数量
    private BigDecimal quantity;

    // 委托价格
    private BigDecimal price;

}
