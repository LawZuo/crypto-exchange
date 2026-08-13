package coin.exchange.api.market.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WsKLineVo {

    private Long t; // 时间

    private Long T; //

    private String s; // 币种

    private String i; // 类型 1m

    private BigDecimal o;

    private BigDecimal c;

    private BigDecimal h;

    private BigDecimal l;

    private BigDecimal v;

    private BigDecimal q;

    private BigDecimal V;

    private BigDecimal Q;
}
