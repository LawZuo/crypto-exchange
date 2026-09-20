package coin.exchange.web.dto;

public record LoginRequest(
        String username,
        String password,
        String email,
        int loginType // 1 账号 2 邮箱
) {}
