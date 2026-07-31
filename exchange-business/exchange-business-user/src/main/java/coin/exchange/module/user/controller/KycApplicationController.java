package coin.exchange.module.user.controller;

import coin.exchange.api.user.dto.KycApplicationDto;
import coin.exchange.common.core.response.R;
import coin.exchange.common.security.annotation.Idempotent;
import coin.exchange.module.user.domain.KycApplicationDo;
import coin.exchange.module.user.service.KycApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户KYC")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/kyc")
@Slf4j
public class KycApplicationController {
    private final KycApplicationService kycApplicationService;

    @Operation(summary = "创建KYC申请")
    @PostMapping
    @Idempotent(prefix = "kyc:create", key = "#p0.userId", expire = 10, message = "KYC申请正在处理，请勿重复提交")
    public R<String> createKycApplication(@Valid @RequestBody KycApplicationDto kycApplication) {
        Long application = kycApplicationService.createKycApplication(kycApplication);
        log.info("【KYC】新增KYC成功，userId:{}", kycApplication.getUserId());
        return R.success(String.valueOf(application));
    }

    @Operation(summary = "修改KYC申请")
    @PutMapping("/{id}")
    @Idempotent(prefix = "kyc:update", expire = 10, message = "KYC修改正在处理，请勿重复提交")
    public R<String> updateKycApplication(
            @PathVariable("id") Long id,
            @Valid @RequestBody KycApplicationDto kycApplication
    ) {
        Long application = kycApplicationService.updateKycApplication(id, kycApplication);
        log.info("【KYC】修改KYC成功，id:{}，userId:{}", id, kycApplication.getUserId());
        return R.success(String.valueOf(application));
    }

    @Operation(summary = "删除KYC申请")
    @DeleteMapping("/{id}")
    @Idempotent(prefix = "kyc:delete", expire = 10, message = "KYC删除正在处理，请勿重复提交")
    public R<String> deleteKycApplication(@PathVariable("id") Long id) {
        Long application = kycApplicationService.deleteKycApplication(id);
        log.info("【KYC】删除KYC成功，id:{}", id);
        return R.success(String.valueOf(application));
    }

    @Operation(summary = "获取KYC申请")
    @GetMapping("/{userId}")
    public R<KycApplicationDo> getKycApplication(@PathVariable("userId") Long userId) {
        KycApplicationDo kycApplication = kycApplicationService.getKycApplication(userId);
        return R.success(kycApplication);
    }

    @Operation(summary = "更新KYC申请状态")
    @PutMapping("/{id}/{status}")
    @Idempotent(prefix = "kyc:status", expire = 10, message = "KYC状态更新正在处理，请勿重复提交")
    public R<String> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") int status
    ) {
        kycApplicationService.updateStatus(id, status);
        log.info("【KYC】更新KYC状态成功，id：{}, status: {}", id, status);
        return R.success("更新成功");
    }
}
