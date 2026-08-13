package coin.exchange.api.market.config;

import coin.exchange.api.market.factory.RemoteBinanceDataSourceFallbackFactory;
import coin.exchange.api.market.factory.RemoteMarketFallbackFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExchangeApiMarketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RemoteMarketFallbackFactory remoteMarketFallbackFactory() {
        return new RemoteMarketFallbackFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteBinanceDataSourceFallbackFactory remoteBinanceDataSourceFallbackFactory() {
        return new RemoteBinanceDataSourceFallbackFactory();
    }
}
