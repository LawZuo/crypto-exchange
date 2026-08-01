package coin.exchange.common.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage<T> implements Serializable {
    /** 全局唯一ID，用于幂等去重 */
    private String messageId;
    /** 业务类型标识，如 EMAIL_CODE_SEND, SMS_VERIFY */
    private String bizType;
    /** 实际业务载荷 */
    private T payload;
    /** 创建时间戳 */
    private long timestamp;
}
