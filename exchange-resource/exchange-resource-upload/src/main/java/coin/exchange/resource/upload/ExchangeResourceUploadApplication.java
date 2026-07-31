package coin.exchange.resource.upload;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = DynamicDataSourceAutoConfiguration.class)
public class ExchangeResourceUploadApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ExchangeResourceUploadApplication.class, args);
    }
}
