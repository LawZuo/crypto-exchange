package coin.exchange.business.market.service;

import coin.exchange.api.market.model.MarketRankVo;

import java.util.List;

public interface MarketRankingService {
    List<MarketRankVo> listGainers(Integer limit);
    List<MarketRankVo> listLosers(Integer limit);
    List<MarketRankVo> listVolume(Integer limit);
}
