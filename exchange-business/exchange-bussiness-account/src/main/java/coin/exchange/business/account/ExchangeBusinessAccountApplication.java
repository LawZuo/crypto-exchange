package coin.exchange.business.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "coin.exchange.api.account.service")
public class ExchangeBusinessAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeBusinessAccountApplication.class, args);
    }
}
