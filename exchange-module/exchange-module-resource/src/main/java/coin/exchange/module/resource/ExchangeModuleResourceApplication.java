package coin.exchange.module.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Resource 子模块的统一启动入口。
 */
@SpringBootApplication(
        scanBasePackages = {
                "coin.exchange.resource.mail",
                "coin.exchange.resource.upload"
        },
        excludeName = {
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                "com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration"
        }
)
public class ExchangeModuleResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeModuleResourceApplication.class, args);
    }
}
