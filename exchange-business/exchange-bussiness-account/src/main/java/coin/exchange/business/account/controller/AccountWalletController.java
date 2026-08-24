package coin.exchange.business.account.controller;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.business.account.domain.AccountWalletDo;
import coin.exchange.business.account.service.AccountBalanceLogService;
import coin.exchange.business.account.service.AccountWalletService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 账户钱包接口
 */
@Slf4j
@RequestMapping("/account/wallet")
@RestController
@RequiredArgsConstructor
public class AccountWalletController {

    private final AccountWalletService accountWalletService;

    /**
     * 获取用户钱包余额
     * @param userId
     */
    @GetMapping("/balance/{userId}")
    public R<List<AccountWalletVo>> getBalance(
            @PathVariable(name = "userId", required = true) Long userId
    ) {
        List<AccountWalletDo> walletList = accountWalletService.listWallets(userId);
        List<AccountWalletVo> result = new ArrayList<>();
        BeanUtil.copyProperties(walletList, AccountWalletVo.class);
        return R.success(result);
    }

}
