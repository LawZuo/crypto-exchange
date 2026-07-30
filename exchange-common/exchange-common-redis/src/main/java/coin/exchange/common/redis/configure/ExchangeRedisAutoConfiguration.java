package coin.exchange.common.redis.configure;

import coin.exchange.common.redis.service.RedisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
@Import(RedisConfig.class)
public class ExchangeRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisService redisService(@Qualifier("exchangeRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return new RedisService(redisTemplate);
    }
}
