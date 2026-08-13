package coin.exchange.business.market.service;

import coin.exchange.business.market.domain.MarketSymbolDo;

import java.util.List;

public interface MarketSymbolService {

    MarketSymbolDo getSymbol(String symbol);

    List<MarketSymbolDo> listSymbols(Integer status);
}
