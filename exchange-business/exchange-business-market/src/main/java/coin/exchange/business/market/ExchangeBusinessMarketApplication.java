package coin.exchange.business.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "coin.exchange.api.market.service")
@SpringBootApplication
public class ExchangeBusinessMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeBusinessMarketApplication.class, args);
    }
}
