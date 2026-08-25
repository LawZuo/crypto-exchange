package coin.exchange.api.resource.factory;


import coin.exchange.api.resource.dto.EmailDto;
import coin.exchange.api.resource.model.UploadVo;
import coin.exchange.api.resource.service.RemoteResourceService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class RemoteResourceFallbackFactory implements FallbackFactory<RemoteResourceService> {
    @Override
    public RemoteResourceService create(Throwable throwable) {
        log.error("【Feign异常】资源服务调用失败:{}", throwable.getMessage());
        return new RemoteResourceService() {

            @Override
            public R<UploadVo> uploadImageByKyc(MultipartFile file) {
                return R.fail("【Feign异常】调取KYC头像上传失败" + throwable.getMessage());
            }

            @Override
            public R<UploadVo> uploadImageByAvatar(MultipartFile file) {
                return R.fail("【Feign异常】调取用户头像上传失败" + throwable.getMessage());
            }

            @Override
            public R<Void> sendVerificationCodeEmail(EmailDto emailDto) {
                return R.fail("【Feign异常】调取发送邮箱验证吗失败" + throwable.getMessage());
            }
        };
    }
}
