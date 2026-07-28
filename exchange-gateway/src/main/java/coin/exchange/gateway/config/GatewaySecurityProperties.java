package coin.exchange.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security")
@Data
public class GatewaySecurityProperties {

    private List<String> ignorePaths = new ArrayList<>(List.of(
            "/api/crypto-exchange/auth/login",
            "/api/crypto-exchange/auth/register",
            "/api/crypto-exchange/auth/logout"
    ));
}
