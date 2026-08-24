package coin.exchange.business.account.controller;

import coin.exchange.api.account.dto.AccountRechargeRecordListDto;
import coin.exchange.business.account.domain.AccountRechargeRecordDo;
import coin.exchange.business.account.service.AccountRechargeService;
import coin.exchange.common.core.vo.PageResultVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 充值服务
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/account/recharge")
public class AccountRechargeController {

    private final AccountRechargeService accountRechargeService;

    /**
     * 分页查询充值记录
     */
    @GetMapping("/page")
    public PageResultVo<AccountRechargeRecordDo> GetRechargeRecordsListByPage(
            @ModelAttribute AccountRechargeRecordListDto dto
    ) {
        return accountRechargeService.listRechargeRecords(
                dto.getPageNum(),
                dto.getPageSize(),
                dto.getUserId(),
                dto.getUid(),
                dto.getCurrency()
        );
    }

    /**
     * TODO 用户层
     * 1. 用户充值申请
     * 2. 用户查看充值记录
     */
    public void recharge() {

    }

    /**
     * TODO admin层
     * 1. 充值记录
     * 2. 充值审核
     */
}
