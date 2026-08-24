package coin.exchange.business.market.service;

import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketStreamMessageVo;

import java.util.Collection;

/**
 * 行情缓存服务 - 从缓存中获取行情数据
 */
public interface MarketCacheService {

    /**
     * 获取24小时
     */
    Object getTicker(String symbol);

    Object getDepth(String symbol);

    Object getTrade(String symbol);

    Object getKline(String symbol, String interval);

    MarketCacheSnapshotVo getSnapshot(String symbol, String interval, Collection<String> types);

    void update(MarketStreamMessageVo message);

    void warmUp(Collection<String> symbols, String interval);
}
