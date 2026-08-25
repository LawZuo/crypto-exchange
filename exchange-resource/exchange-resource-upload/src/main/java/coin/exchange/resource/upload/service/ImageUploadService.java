package coin.exchange.resource.upload.service;

import coin.exchange.api.resource.model.UploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {


    /**
     * 上传图片
     */
    public UploadVo upload(MultipartFile file, String path);
}
