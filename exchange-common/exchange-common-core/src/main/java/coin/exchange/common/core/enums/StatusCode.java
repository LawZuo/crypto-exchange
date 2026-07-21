package coin.exchange.common.core.enums;

import lombok.Getter;

/**
 * 状态码
 */

@Getter
public enum StatusCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ============ 用户（10000-10999）============
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_ALREADY_EXISTS(10002, "用户已存在"),
    USER_PASSWORD_ERROR(10003, "用户名或密码错误"),
    USER_DISABLED(10004, "账号已被禁用"),
    USER_KYC_REQUIRED(10005, "请先完成实名认证"),
    USER_TOKEN_EXPIRED(10006, "Token 已过期"),
    USER_TOKEN_INVALID(10007, "Token 无效"),

    // ============ 账户（11000-11999）============
    ACCOUNT_NOT_FOUND(11001, "账户不存在"),
    ACCOUNT_INSUFFICIENT_BALANCE(11002, "账户余额不足"),
    ACCOUNT_FROZEN(11003, "账户已冻结"),
    ACCOUNT_WITHDRAW_DAILY_LIMIT(11004, "已超过单日提现额度"),
    ACCOUNT_TRANSFER_FAILED(11005, "转账失败"),

    // ============ 订单（12000-12999）============
    ORDER_NOT_FOUND(12001, "订单不存在"),
    ORDER_ALREADY_CANCELLED(12002, "订单已撤销"),
    ORDER_ALREADY_FILLED(12003, "订单已成交"),
    ORDER_PRICE_OUT_OF_RANGE(12004, "订单价格超出允许范围"),
    ORDER_QUANTITY_INVALID(12005, "订单数量无效"),
    ORDER_RATE_LIMIT(12006, "下单频率超限"),
    ORDER_SYMBOL_NOT_SUPPORTED(12007, "不支持的交易对"),
    ORDER_LEVERAGE_EXCEED(12008, "杠杆倍数超出允许范围"),
    ORDER_AMOUNT_TOO_SMALL(12009, "订单金额小于最小交易额"),

    // ============ 业务通用错误（99999）============
    UNKNOWN_ERROR(99999, "未知错误");

    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
