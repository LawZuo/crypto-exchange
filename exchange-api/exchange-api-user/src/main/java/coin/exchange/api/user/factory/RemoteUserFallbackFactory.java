package coin.exchange.api.user.factory;

import coin.exchange.api.user.dto.LoginRecordDto;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.api.user.service.RemoteUserService;
import coin.exchange.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService> {

    @Override
    public RemoteUserService create(Throwable throwable) {
        log.error("用户服务调用失败:{}", throwable.getMessage());
        return new RemoteUserService() {
            @Override
            public R<UserVo> getUserInfo(String username)
            {
                return R.fail("Feign调取用户信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Long> registerUser(RegisterUserDto dto) {
                return R.fail("Feign调取注册用户服务失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> recordLogin(String source, LoginRecordDto dto) {
                return R.fail("Feign记录用户登录信息失败:" + throwable.getMessage());
            }
        };

    }
}
