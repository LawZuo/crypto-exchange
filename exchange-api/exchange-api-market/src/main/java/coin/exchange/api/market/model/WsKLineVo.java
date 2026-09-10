package coin.exchange.api.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WsKLineVo {

    private Long t; // 时间

    private Long T; //

    private String s; // 币种

    private String i; // 类型 1m

    private BigDecimal o; // ✅ 开盘价 - BigDecimal!

    private BigDecimal c; // ✅ 收盘价

    private BigDecimal h; // ✅ 最高价

    private BigDecimal l; // ✅ 最低价

    private BigDecimal v; // ✅ 成交量

    private BigDecimal q; // 成交额

    private BigDecimal V; // 主动买入量

    private BigDecimal Q; // 主动买入额
}
