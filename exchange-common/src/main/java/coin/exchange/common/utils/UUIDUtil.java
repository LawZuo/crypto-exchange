package coin.exchange.common.utils;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.UUID;

@Component
public class UUIDUtil {

    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    // 更安全：带时间戳 + 随机
    public String generateSecure() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(
                        (System.currentTimeMillis() + ":" + UUID.randomUUID()).getBytes()
                );
    }
}
