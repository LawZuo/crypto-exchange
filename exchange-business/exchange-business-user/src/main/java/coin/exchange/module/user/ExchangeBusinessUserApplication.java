package coin.exchange.module.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ExchangeBusinessUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeBusinessUserApplication.class, args);
    }
}
