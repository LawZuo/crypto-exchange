package coin.exchange.resource.upload.service.impl;

import coin.exchange.api.resource.model.UploadVo;
import coin.exchange.resource.upload.service.ImageUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-path:/files/}")
    private String accessPath;

    @Override
    public UploadVo upload(MultipartFile file, String path) {

        log.info("上传图片信息: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("上传图片不能为空");
            }
            String contentType = file.getContentType();
            if (!StringUtils.hasText(contentType) || !contentType.startsWith("image")) {
                throw new IllegalArgumentException("上传图片格式错误");
            }

            // 1. 按日期分目录，避免单目录文件过多
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String bizPath = normalizeBizPath(path);
            Path dirPath = Paths.get(uploadPath).toAbsolutePath().normalize()
                    .resolve("image")
                    .resolve(bizPath)
                    .resolve(dateDir);
            Files.createDirectories(dirPath);

            // 2. 生成唯一文件名，防止覆盖 + 保留原始扩展名
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = StringUtils.getFilenameExtension(originalName);
            String newFileName = StringUtils.hasText(extension)
                    ? UUID.randomUUID() + "." + extension
                    : UUID.randomUUID().toString();

            // 3. 写入磁盘
            Path targetPath = dirPath.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("图片上传成功: {}", targetPath);
            String imageUrl = normalizeAccessPath(accessPath) + "image/" + bizPath + "/" + dateDir + "/" + newFileName;
            log.info("图片访问地址: {}", imageUrl);

            UploadVo uploadVo = new UploadVo();
            uploadVo.setUrl(imageUrl);
            uploadVo.setName(file.getOriginalFilename());
            uploadVo.setType(contentType);
            return uploadVo;
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new RuntimeException(e);
        }
    }

    private String normalizeBizPath(String path) {
        if (!StringUtils.hasText(path)) {
            return "default";
        }
        return path.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String normalizeAccessPath(String path) {
        String normalized = StringUtils.hasText(path) ? path.trim() : "/files/";
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
