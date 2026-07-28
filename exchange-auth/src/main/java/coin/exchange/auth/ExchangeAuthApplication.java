package coin.exchange.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "coin.exchange.api")
public class ExchangeAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeAuthApplication.class, args);
    }
}
