package coin.exchange.common.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Jwt token生成器
 */

public class JwtUtil {

    // 生产环境放配置里
    private static final String SECRET = "your-256-bit-secret-key-must-be-long-enough";
    public static final long EXPIRE_MS = 24 * 60 * 60 * 1000L; // 1 天
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /** 生成 token */
    public static String generate(Long userId, String username, Date now) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_MS))
                .signWith(key)
                .compact();
    }
    /** 解析 token */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /** 校验 */
    public static boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 过期时间 */
    public static Long getExpireMs(Date now) {
        return now.getTime() + EXPIRE_MS;
    }
}
