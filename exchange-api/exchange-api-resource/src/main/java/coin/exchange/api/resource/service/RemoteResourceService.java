package coin.exchange.api.resource.service;

import coin.exchange.api.resource.dto.EmailDto;
import coin.exchange.api.resource.model.UploadVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "exchange-module-resource",
        value="service-resource"
)
public interface RemoteResourceService {

    // 上传用户KYC图片
    @PostMapping("/upload/image/user/kyc")
    R<UploadVo> uploadImageByKyc(@RequestParam("file") MultipartFile file);

    // 用户头像图片
    @PostMapping("/upload/image/user/avatar")
    R<UploadVo> uploadImageByAvatar(@RequestParam("file") MultipartFile file);

    // 发送邮件验证码
    @PostMapping("/email/verification/code")
    R<Void> sendVerificationCodeEmail(@RequestBody EmailDto emailDto);
}
