package coin.exchange.web.controller;

import coin.exchange.api.resource.dto.EmailDto;
import coin.exchange.api.resource.model.UploadVo;
import coin.exchange.api.resource.service.RemoteResourceService;
import coin.exchange.common.core.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class WebResourceController {

    private final RemoteResourceService remoteResourceService;

    @PostMapping(value = "/upload/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<UploadVo> uploadKyc(@RequestPart("file") MultipartFile file) {
        return remoteResourceService.uploadImageByKyc(file);
    }

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<UploadVo> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return remoteResourceService.uploadImageByAvatar(file);
    }

    @PostMapping("/email/code")
    public R<Void> sendEmailCode(@RequestBody EmailDto dto) {
        return remoteResourceService.sendVerificationCodeEmail(dto);
    }
}
