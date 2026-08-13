package coin.exchange.api.market.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MarketStreamMessageVo implements Serializable {

    private String source;
    private String type;
    private String symbol;
    private String interval;
    private Object payload;
    private Long timestamp;
}
