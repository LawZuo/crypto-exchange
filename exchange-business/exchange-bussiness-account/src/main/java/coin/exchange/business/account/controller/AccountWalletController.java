package coin.exchange.business.account.controller;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.api.account.dto.CreateAccountWalletDto;
import coin.exchange.api.account.model.AccountWalletVo;
import coin.exchange.business.account.domain.AccountWalletDo;
import coin.exchange.business.account.service.AccountBalanceLogService;
import coin.exchange.business.account.service.AccountWalletService;
import coin.exchange.common.core.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
     * 幂等创建钱包；已存在时直接返回已有钱包ID。
     */
    @PostMapping("/create")
    public R<Long> createWallet(@Valid @RequestBody CreateAccountWalletDto walletDto) {
        return R.success(accountWalletService.getOrCreateWallet(walletDto));
    }

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

    /**
     * 冻结资产
     */
    @PostMapping("/frozen")
    public R<String> frozenAssets(@RequestBody AccountFrozenAssetsDto accountFrozenAssetsDto) {
        try {
            accountWalletService.frozenBalance(accountFrozenAssetsDto);
            return R.success(null);
        } catch (RuntimeException e) {
            return R.fail("冻结资产失败:" + e);
        }
    }

    /**
     * 扣除资产
     */
    @PostMapping("/deduct")
    public R<String> deductAssets(@RequestBody AccountFrozenAssetsDto assetsDto) {
        try {
            accountWalletService.deductBalance(assetsDto);
            return R.success(null);
        } catch (RuntimeException e) {
            return R.fail("扣除资产失败:" + e);
        }
    }
}
