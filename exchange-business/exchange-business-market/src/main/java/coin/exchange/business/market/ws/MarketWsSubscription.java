package coin.exchange.business.market.ws;

import lombok.Data;

import java.util.List;

@Data
public class MarketWsSubscription {

    private String action;
    private String symbol;
    private String interval;
    private List<String> types;
}
