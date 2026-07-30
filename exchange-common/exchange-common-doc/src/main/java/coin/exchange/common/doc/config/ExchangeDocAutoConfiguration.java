package coin.exchange.common.doc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(ExchangeDocProperties.class)
public class ExchangeDocAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI exchangeOpenApi(ExchangeDocProperties properties) {
        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion()));

        if (properties.isBearerAuthEnabled()) {
            String bearerAuthName = properties.getBearerAuthName();
            openApi.addSecurityItem(new SecurityRequirement().addList(bearerAuthName))
                    .components(new Components()
                            .addSecuritySchemes(bearerAuthName, new SecurityScheme()
                                    .name(bearerAuthName)
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));
        }

        return openApi;
    }
}
