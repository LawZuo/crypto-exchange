package coin.exchange.common.security.config;

import coin.exchange.common.redis.configure.ExchangeRedisAutoConfiguration;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.common.security.aspect.IdempotentAspect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(ExchangeRedisAutoConfiguration.class)
public class IdempotentAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedisService.class)
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect(RedisService redisService, ObjectMapper objectMapper) {
        return new IdempotentAspect(redisService, objectMapper);
    }
}
