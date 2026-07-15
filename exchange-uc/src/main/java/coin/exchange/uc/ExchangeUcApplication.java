package coin.exchange.uc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "coin.exchange")
public class ExchangeUcApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeUcApplication.class, args);
    }
}
