package coin.exchange.business.account.controller;

import coin.exchange.api.account.dto.AccountWithdrawRecordListDto;
import coin.exchange.business.account.domain.AccountWithdrawRecordDo;
import coin.exchange.business.account.service.AccountWithdrawService;
import coin.exchange.common.core.vo.PageResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提现服务。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/account/withdraw")
public class AccountWithdrawController {

    private final AccountWithdrawService accountWithdrawService;

    /**
     * 分页查询提现记录。
     */
    @GetMapping("/page")
    public PageResultVo<AccountWithdrawRecordDo> getWithdrawRecordsListByPage(
            @ModelAttribute AccountWithdrawRecordListDto dto) {
        return accountWithdrawService.listWithdrawRecords(
                dto.getPageNum(),
                dto.getPageSize(),
                dto.getUserId(),
                dto.getCurrency()
        );
    }
}
