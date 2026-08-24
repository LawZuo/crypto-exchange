package coin.exchange.module.datasrouce;

import coin.exchange.module.datasrouce.adapter.BinanceWsClient;
import coin.exchange.module.datasrouce.config.BinanceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration"
})
@RequiredArgsConstructor
@EnableConfigurationProperties(BinanceProperties.class)
@EnableFeignClients(basePackages = "coin.exchange.api.market.service")
@EnableScheduling
public class ExchangeModuleDataSourceApplication implements CommandLineRunner {

    private final BinanceProperties binanceProperties;
    private final BinanceWsClient binanceWsClient;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ExchangeModuleDataSourceApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Override
    public void run(String... args) {
        if (!binanceProperties.isEnabled()) {
            log.info("Binance WebSocket 未启用，跳过启动");
            return;
        }
        binanceWsClient.start();
    }
}
