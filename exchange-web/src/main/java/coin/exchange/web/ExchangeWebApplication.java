package coin.exchange.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"coin.exchange.api", "coin.exchange.web.client"})
public class ExchangeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeWebApplication.class, args);
    }
}
