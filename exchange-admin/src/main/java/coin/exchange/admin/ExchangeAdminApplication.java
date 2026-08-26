package coin.exchange.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "coin.exchange.api")
public class ExchangeAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeAdminApplication.class, args);
    }
}
