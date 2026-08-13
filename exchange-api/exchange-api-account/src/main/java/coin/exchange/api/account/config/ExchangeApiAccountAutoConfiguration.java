package coin.exchange.api.account.config;

import coin.exchange.api.account.factory.RemoteAccountFallbackFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExchangeApiAccountAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RemoteAccountFallbackFactory remoteAccountFallbackFactory() {
        return new RemoteAccountFallbackFactory();
    }
}
