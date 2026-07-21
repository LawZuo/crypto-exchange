package coin.exchange.common.core.utils;

import java.util.Base64;
import java.util.UUID;


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

    /**
     * 返回纯数字8位UID
     */
    public static String generate8DigitId() {
        UUID uuid = UUID.randomUUID();
        // 用高低64位异或混合熵源，避免仅取低位导致的周期性
        long mixed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        // Math.abs(Long.MIN_VALUE) 仍为负数，需特殊处理
        long positive = (mixed == Long.MIN_VALUE) ? 0 : Math.abs(mixed);
        return String.format("%08d", positive % 100_000_000L);
    }
}
