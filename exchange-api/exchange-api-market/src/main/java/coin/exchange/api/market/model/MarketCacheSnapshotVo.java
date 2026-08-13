package coin.exchange.api.market.model;

import lombok.Data;

@Data
public class MarketCacheSnapshotVo {

    private String symbol;
    private String interval;
    private Object ticker;
    private Object depth;
    private Object trade;
    private Object kline;
}
