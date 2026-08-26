package coin.exchange.api.resource.service;

import coin.exchange.api.resource.dto.EmailDto;
import coin.exchange.api.resource.model.UploadVo;
import coin.exchange.common.core.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "exchange-service-resource"
)
public interface RemoteResourceService {

    // 上传用户KYC图片
    @PostMapping(value = "/upload/image/user/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<UploadVo> uploadImageByKyc(@RequestPart("file") MultipartFile file);

    // 用户头像图片
    @PostMapping(value = "/upload/image/user/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<UploadVo> uploadImageByAvatar(@RequestPart("file") MultipartFile file);

    // 发送邮件验证码
    @PostMapping("/email/verification/code")
    R<Void> sendVerificationCodeEmail(@RequestBody EmailDto emailDto);
}
