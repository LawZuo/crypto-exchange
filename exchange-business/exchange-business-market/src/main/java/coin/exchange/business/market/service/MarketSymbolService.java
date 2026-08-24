package coin.exchange.business.market.service;

import coin.exchange.business.market.domain.MarketSymbolDo;

import java.util.List;

/**
 * 交易对服务层
 */
public interface MarketSymbolService {

    MarketSymbolDo getSymbol(String symbol);

    List<MarketSymbolDo> listSymbols(Integer status);
}
