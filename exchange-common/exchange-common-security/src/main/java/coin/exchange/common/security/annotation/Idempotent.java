package coin.exchange.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交注解。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Redis key 前缀，建议按业务域命名。
     */
    String prefix() default "idempotent:submit";

    /**
     * 自定义业务 key，支持 SpEL，例如：#p0.username、#request.requestURI。
     * 为空时使用请求路径、参数和方法入参自动生成指纹。
     */
    String key() default "";

    /**
     * 客户端幂等请求头。传入后会优先参与幂等 key 生成。
     */
    String header() default "Idempotency-Key";

    /**
     * 幂等 key 过期时间。
     */
    long expire() default 5;

    /**
     * 过期时间单位。
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 重复提交提示信息。
     */
    String message() default "请勿重复提交";

    /**
     * 业务执行失败时是否释放幂等 key，避免失败后无法立即重试。
     */
    boolean releaseOnFailure() default true;
}
