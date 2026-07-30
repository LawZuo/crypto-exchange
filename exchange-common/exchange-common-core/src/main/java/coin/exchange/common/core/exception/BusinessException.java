package coin.exchange.common.core.exception;

import coin.exchange.common.core.enums.StatusCode;
import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final StatusCode statusCode;

    public BusinessException(String message) {
        this(StatusCode.FAIL, message);
    }

    public BusinessException(StatusCode statusCode) {
        this(statusCode, statusCode.getMessage());
    }

    public BusinessException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
