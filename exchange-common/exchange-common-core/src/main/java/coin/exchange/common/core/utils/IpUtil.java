package coin.exchange.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.http.HttpRequest;

/**
 * 获取用户IP地址
 */
public class IpUtil {

    private static final String REAL_IP_HEADER = "X-Real-Client-IP";

    /**
     * 获取用户真实IP（适配Spring Cloud网关透传场景）
     */
    public static String getClientIp(HttpServletRequest request) {
        return getClientIp(
                request.getHeader(REAL_IP_HEADER),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr()
        );
    }

    /**
     * 获取用户真实IP（适配 WebFlux 等非 Servlet 请求对象）
     */
    public static String getClientIp(String realClientIp, String forwardedFor, String realIp, String remoteAddr) {
        if (isValidIp(realClientIp)) {
            return realClientIp;
        }
        if (isValidIp(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        if (isValidIp(realIp)) {
            return realIp.split(",")[0].trim();
        }
        return remoteAddr;
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip);
    }
}
