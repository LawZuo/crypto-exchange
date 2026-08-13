package coin.exchange.business.market.service;

import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketStreamMessageVo;

import java.util.Collection;

public interface MarketCacheService {

    Object getTicker(String symbol);

    Object getDepth(String symbol);

    Object getTrade(String symbol);

    Object getKline(String symbol, String interval);

    MarketCacheSnapshotVo getSnapshot(String symbol, String interval, Collection<String> types);

    void update(MarketStreamMessageVo message);

    void warmUp(Collection<String> symbols, String interval);
}
