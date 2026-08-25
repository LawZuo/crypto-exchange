package coin.exchange.api.resource.config;

import coin.exchange.api.resource.factory.RemoteResourceFallbackFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExchangeApiResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RemoteResourceFallbackFactory remoteResourceService() {
        return new RemoteResourceFallbackFactory();
    }
}
