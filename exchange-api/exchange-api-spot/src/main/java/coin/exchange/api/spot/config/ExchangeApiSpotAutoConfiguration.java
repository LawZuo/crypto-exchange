package coin.exchange.api.spot.config;

import coin.exchange.api.spot.factory.RemoteSpotFallbackFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExchangeApiSpotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RemoteSpotFallbackFactory remoteSpotFallbackFactory() {
        return new RemoteSpotFallbackFactory();
    }
}
