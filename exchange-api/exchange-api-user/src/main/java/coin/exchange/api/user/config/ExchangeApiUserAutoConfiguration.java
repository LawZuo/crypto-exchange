package coin.exchange.api.user.config;

import coin.exchange.api.user.factory.RemoteUserFallbackFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExchangeApiUserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RemoteUserFallbackFactory remoteUserFallbackFactory() {
        return new RemoteUserFallbackFactory();
    }
}
