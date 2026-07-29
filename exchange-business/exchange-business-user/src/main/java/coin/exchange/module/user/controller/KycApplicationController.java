package coin.exchange.module.user.controller;

import coin.exchange.api.user.dto.KycApplicationDto;
import coin.exchange.common.core.response.R;
import coin.exchange.common.security.annotation.Idempotent;
import coin.exchange.module.user.domain.KycApplicationDo;
import coin.exchange.module.user.service.KycApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-kyc")
@Slf4j
public class KycApplicationController {
    private final KycApplicationService kycApplicationService;

    @PostMapping
    @Idempotent(prefix = "kyc:create", key = "#p0.userId", expire = 10, message = "KYC申请正在处理，请勿重复提交")
    public R<String> createKycApplication(@RequestBody KycApplicationDto kycApplication) {
        try {
            Long application = kycApplicationService.createKycApplication(kycApplication);
            log.info("【KYC】新增KYC成功，userId:{}", kycApplication.getUserId());
            return R.success(String.valueOf(application));
        } catch (RuntimeException e) {
            log.error("【KYC】新增KYC失败：userId:{}, message:{}", kycApplication.getUserId(), e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Idempotent(prefix = "kyc:update", expire = 10, message = "KYC修改正在处理，请勿重复提交")
    public R<String> updateKycApplication(
            @PathVariable("id") Long id,
            @RequestBody KycApplicationDto kycApplication
    ) {
        try {
            Long application = kycApplicationService.updateKycApplication(id, kycApplication);
            log.info("【KYC】修改KYC成功，id:{}，userId:{}", id, kycApplication.getUserId());
            return R.success(String.valueOf(application));
        } catch (RuntimeException e) {
            log.error("【KYC】修改KYC失败：id:{}，userId:{}, message:{}", id, kycApplication.getUserId(), e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Idempotent(prefix = "kyc:delete", expire = 10, message = "KYC删除正在处理，请勿重复提交")
    public R<String> deleteKycApplication(@PathVariable("id") Long id) {
        try {
            Long application = kycApplicationService.deleteKycApplication(id);
            log.info("【KYC】删除KYC成功，id:{}", id);
            return R.success(String.valueOf(application));
        } catch (RuntimeException e) {
            log.error("【KYC】删除KYC失败：id:{}，message:{}", id, e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public R<KycApplicationDo> getKycApplication(@PathVariable("userId") Long userId) {
        try {
            KycApplicationDo kycApplication = kycApplicationService.getKycApplication(userId);
            return R.success(kycApplication);
        } catch (RuntimeException e) {
            log.error("【KYC】查询KYC失败：{}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}/{status}")
    @Idempotent(prefix = "kyc:status", expire = 10, message = "KYC状态更新正在处理，请勿重复提交")
    public R<String> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") int status
    ) {
        try {
            kycApplicationService.updateStatus(id, status);
            log.info("【KYC】更新KYC状态成功，id：{}, status: {}", id, status);
            return R.success("更新成功");
        } catch (RuntimeException e) {
            log.error("【KYC】更新KYC状态失败：{}", e.getMessage());
            return R.fail("更新失败");
        }
    }
}
