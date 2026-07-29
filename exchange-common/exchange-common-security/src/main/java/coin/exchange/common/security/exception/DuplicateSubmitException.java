package coin.exchange.common.security.exception;

/**
 * 重复提交异常。
 */
public class DuplicateSubmitException extends RuntimeException {

    public DuplicateSubmitException(String message) {
        super(message);
    }
}
