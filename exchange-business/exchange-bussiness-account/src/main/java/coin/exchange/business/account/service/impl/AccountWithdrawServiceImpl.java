package coin.exchange.business.account.service.impl;

import cn.hutool.db.PageResult;
import coin.exchange.business.account.domain.AccountWithdrawRecordDo;
import coin.exchange.business.account.mapper.AccountWithdrawRecordMapper;
import coin.exchange.business.account.service.AccountWithdrawService;
import coin.exchange.common.core.vo.PageResultVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 提现服务实现类
 */

@Slf4j
@Service
public class AccountWithdrawServiceImpl
        extends ServiceImpl<AccountWithdrawRecordMapper, AccountWithdrawRecordDo>
        implements AccountWithdrawService {

    @Override
    public PageResultVo<AccountWithdrawRecordDo> listWithdrawRecords(
            Integer pageNum, Integer pageSize, Long userId, String currency) {
        try {
            int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
            int currentPageSize = pageSize == null || pageSize < 1
                    ? PageResult.DEFAULT_PAGE_SIZE
                    : pageSize;

            LambdaQueryWrapper<AccountWithdrawRecordDo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(userId != null, AccountWithdrawRecordDo::getUserId, userId);
            queryWrapper.eq(StringUtils.hasText(currency), AccountWithdrawRecordDo::getCurrency, currency);
            queryWrapper.orderByDesc(AccountWithdrawRecordDo::getCreatedTime);

            Page<AccountWithdrawRecordDo> result = this.page(
                    new Page<>(currentPage, currentPageSize),
                    queryWrapper
            );

            return new PageResultVo<>(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    result.getRecords()
            );
        } catch (Exception e) {
            log.error("【账户模块】获取提现记录失败", e);
            throw new RuntimeException("获取提现记录失败", e);
        }
    }
}
