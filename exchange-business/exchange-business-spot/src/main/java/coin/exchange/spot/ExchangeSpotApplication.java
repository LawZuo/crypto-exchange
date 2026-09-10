package coin.exchange.spot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "coin.exchange.api.account.service")
@EnableDiscoveryClient
@MapperScan("coin.exchange.spot.mapper")
@SpringBootApplication
public class ExchangeSpotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeSpotApplication.class, args);
    }
}
