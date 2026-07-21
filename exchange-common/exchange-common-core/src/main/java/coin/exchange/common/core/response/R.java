package coin.exchange.common.core.response;

import coin.exchange.common.core.enums.StatusCode;

import java.util.Objects;

public record R<T>(int code, String message, T data) {

    /** 成功 */
    public static final int SUCCESS_CODE = StatusCode.SUCCESS.getCode();
    public static final String SUCCESS_MESSAGE = StatusCode.SUCCESS.getMessage();

    /** 失败 */
    public static final int FAIL_CODE = StatusCode.FAIL.getCode();
    public static final String FAIL_MESSAGE = StatusCode.FAIL.getMessage();

    /**
     * 通用返回成功
     */
    public static <T> R<T> success(T data) {
        return new R<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 通用返回失败
     */
    public static <T> R<T> fail(String message) {
        return new R<>(FAIL_CODE, Objects.requireNonNullElse(message, FAIL_MESSAGE), null);
    }

    /**
     * 全返回
     */
    public static <T> R<T> all(int code, String message, T data) {
        return new R<>(code, message, data);
    }

    public T getData()
    {
        return data;
    }
}

