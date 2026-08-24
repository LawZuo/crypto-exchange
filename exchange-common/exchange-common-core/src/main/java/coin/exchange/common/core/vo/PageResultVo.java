package coin.exchange.common.core.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResultVo<T> {

    private long pageNum; // 当前页码
    private long pageSize; // 每页数量
    private long total; // 总数
    private List<T> records; // 结果集

    public PageResultVo(
            long pageNum,
            long pageSize,
            long total,
            List<T> records
    ) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }
}
