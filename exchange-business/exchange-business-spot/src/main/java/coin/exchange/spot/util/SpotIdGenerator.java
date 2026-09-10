package coin.exchange.spot.util;

import java.security.SecureRandom;

public final class SpotIdGenerator {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private SpotIdGenerator() {
    }

    public static String orderId() {
        return generate("SP_O_");
    }

    public static String tradeId() {
        return generate("SP_T_");
    }

    private static String generate(String prefix) {
        StringBuilder value = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) {
            value.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return value.toString();
    }
}
