package coin.exchange.business.market.service.impl;

import coin.exchange.business.market.domain.MarketSymbolDo;
import coin.exchange.business.market.mapper.MarketSymbolMapper;
import coin.exchange.business.market.service.MarketSymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MarketSymbolServiceImpl implements MarketSymbolService {

    private final MarketSymbolMapper marketSymbolMapper;

    @Override
    public MarketSymbolDo getSymbol(String symbol) {
        if (Objects.isNull(symbol) || symbol.isBlank()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
        return marketSymbolMapper.getBySymbol(normalizeSymbol(symbol));
    }

    @Override
    public List<MarketSymbolDo> listSymbols(Integer status) {
        if (status == null) {
            return marketSymbolMapper.listAll();
        }
        return marketSymbolMapper.listByStatus(status);
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
}
