package coin.exchange.common.doc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "exchange.doc")
public class ExchangeDocProperties {

    /**
     * 文档标题。
     */
    private String title = "Crypto Exchange API";

    /**
     * 文档描述。
     */
    private String description = "Crypto Exchange 接口文档";

    /**
     * 文档版本。
     */
    private String version = "1.0";

    /**
     * 是否启用 Bearer JWT 安全方案。
     */
    private boolean bearerAuthEnabled = true;

    /**
     * 安全方案名称。
     */
    private String bearerAuthName = "BearerAuth";
}
