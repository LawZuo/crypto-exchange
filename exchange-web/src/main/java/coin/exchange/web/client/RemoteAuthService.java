package coin.exchange.web.client;

import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.LoginVo;
import coin.exchange.common.core.response.R;
import coin.exchange.web.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "exchange-service-auth")
public interface RemoteAuthService {

    @PostMapping("/auth/login")
    R<LoginVo> login(@RequestBody LoginRequest request);

    @PostMapping("/auth/register")
    R<Long> register(@RequestBody RegisterUserDto request);

    @PostMapping("/auth/logout")
    R<String> logout();
}
