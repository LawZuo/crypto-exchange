package coin.exchange.business.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "coin.exchange.api.market.service")
public class ExchangeBusinessMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeBusinessMarketApplication.class, args);
    }
}
