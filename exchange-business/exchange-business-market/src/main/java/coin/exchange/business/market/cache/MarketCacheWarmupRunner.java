package coin.exchange.business.market.cache;

import coin.exchange.business.market.domain.MarketSymbolDo;
import coin.exchange.business.market.service.MarketCacheService;
import coin.exchange.business.market.service.MarketSymbolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketCacheWarmupRunner implements CommandLineRunner {

    private final MarketCacheService marketCacheService;
    private final MarketSymbolService marketSymbolService;

    @Value("${exchange.market.cache.default-kline-interval:1m}")
    private String interval;

    @Override
    public void run(String... args) {
        List<String> symbolList = marketSymbolService.listSymbols(1).stream()
                .map(MarketSymbolDo::getSymbol)
                .toList();
        marketCacheService.warmUp(symbolList, interval);
        log.info("【exchange-business-market】行情本地缓存预热完成: symbols={}, interval={}", symbolList, interval);
    }
}
