package coin.exchange.common.core.dto;

import lombok.Data;

/**
 * 分页查询列表基本参数
 */

@Data
public class PageBaseDto {

    // 当前页码
    private int pageNum;

    // 每页数量
    private int pageSize;

    // 用户ID
    private Long userId;

    // 用户uid
    private String uid;
}
