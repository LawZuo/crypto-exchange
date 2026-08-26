package coin.exchange.web.controller;

import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.LoginVo;
import coin.exchange.common.core.response.R;
import coin.exchange.web.client.RemoteAuthService;
import coin.exchange.web.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class WebAuthController {

    private final RemoteAuthService remoteAuthService;

    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody LoginRequest request) {
        return remoteAuthService.login(request);
    }

    @PostMapping("/register")
    public R<Long> register(@RequestBody RegisterUserDto request) {
        return remoteAuthService.register(request);
    }

    @PostMapping("/logout")
    public R<String> logout() {
        return remoteAuthService.logout();
    }
}
