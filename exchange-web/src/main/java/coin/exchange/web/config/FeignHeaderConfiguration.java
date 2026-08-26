package coin.exchange.web.config;

import coin.exchange.common.core.constant.SecurityConstants;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderConfiguration {

    @Bean
    public RequestInterceptor requestHeaderForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            forward(request, template, SecurityConstants.AUTHORIZATION_HEADER);
            forward(request, template, SecurityConstants.DETAILS_USER_ID);
            forward(request, template, SecurityConstants.DETAILS_USERNAME);
            template.header(SecurityConstants.FROM_SOURCE, SecurityConstants.INNER);
        };
    }

    private void forward(HttpServletRequest request, feign.RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (StringUtils.hasText(value)) {
            template.header(headerName, value);
        }
    }
}
