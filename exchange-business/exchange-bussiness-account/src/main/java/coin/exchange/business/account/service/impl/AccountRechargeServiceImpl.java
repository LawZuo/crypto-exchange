package coin.exchange.business.account.service.impl;

import cn.hutool.db.PageResult;
import coin.exchange.business.account.domain.AccountRechargeRecordDo;
import coin.exchange.business.account.mapper.AccountRechargeRecordMapper;
import coin.exchange.business.account.service.AccountRechargeService;
import coin.exchange.common.core.vo.PageResultVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountRechargeServiceImpl extends ServiceImpl<AccountRechargeRecordMapper, AccountRechargeRecordDo> implements AccountRechargeService {

    private final AccountRechargeRecordMapper accountRechargeRecordMapper;

    @Override
    public Long applyRecharge(Long userId, String currency, BigDecimal amount) {
        return 0L;
    }

    @Override
    public PageResultVo<AccountRechargeRecordDo> listRechargeRecords(Integer pageNum, Integer pageSize, Long userId, String uid, String currency) {

        try {
            if (pageNum == null) pageNum = 1;
            if (pageSize == null) pageSize = PageResult.DEFAULT_PAGE_SIZE;

            // 构建查询参数
            LambdaQueryWrapper<AccountRechargeRecordDo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(userId != null, AccountRechargeRecordDo::getUserId, userId);
            queryWrapper.eq(StringUtils.hasText(uid), AccountRechargeRecordDo::getCurrency, currency);

            queryWrapper.orderByDesc(AccountRechargeRecordDo::getCreatedTime);

            Page<AccountRechargeRecordDo> page = new Page<>(pageNum, pageSize);
            Page<AccountRechargeRecordDo> pageList = this.page(page, queryWrapper);

            PageResultVo<AccountRechargeRecordDo> pageResult = new PageResultVo<>(
                    pageList.getCurrent(),
                    pageList.getSize(),
                    pageList.getTotal(),
                    pageList.getRecords()
            );
            log.info("【账户模块】获取充值记录成功: {}", pageResult.getRecords());
            return pageResult;
        } catch (Exception e) {
            log.error("【账户模块】获取充值记录失败:", e);
            throw new RuntimeException(e);
        }
    }
}
