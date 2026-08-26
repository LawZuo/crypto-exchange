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
            "/api/crypto-exchange/web/auth/login",
            "/api/crypto-exchange/web/auth/register",
            "/api/crypto-exchange/web/auth/logout",
            "/api/crypto-exchange/web/market/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/api/crypto-exchange/web/v3/api-docs/**",
            "/api/crypto-exchange/admin/v3/api-docs/**"
    ));
}
