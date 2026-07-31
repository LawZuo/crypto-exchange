package coin.exchange.resource.upload.controller;

import coin.exchange.common.core.response.R;
import coin.exchange.resource.upload.model.UploadVo;
import coin.exchange.resource.upload.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "图片上传接口")
@RequestMapping("/upload/image")
@RestController
@RequiredArgsConstructor
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    @Operation(summary = "用户KYC图片")
    @PostMapping("/user/kyc")
    public R<UploadVo> uploadImageByKyc(@RequestParam("file") MultipartFile file) {
        return R.success(imageUploadService.upload(file, "user/kyc"));
    }

    @Operation(summary = "用户头像图片")
    @PostMapping("/user/avatar")
    public R<UploadVo> uploadImageByAvatar(@RequestParam("file") MultipartFile file) {
        return R.success(imageUploadService.upload(file, "user/avatar"));
    }
}
