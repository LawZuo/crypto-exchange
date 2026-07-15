package coin.exchange.uc.controller;

import coin.exchange.common.utils.UUIDUtil;
import coin.exchange.uc.dto.RegisterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用户鉴权
 */
@RequestMapping("/api/crypto-exchange/user/auth")
@RestController
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UUIDUtil uuidUtil;

    @PostMapping("/login")
    public String login(
            @RequestBody RegisterDto registerDto
    ) {
        log.info("【用户登录】账号：{}，密码：{}", registerDto.getUsername(), registerDto.getPassword());

        // 生成token
        Long userId = new SecureRandom().nextLong() & Long.MAX_VALUE;
        String token = uuidUtil.generateSecure();

        // 将账号密码转换为token存入redis
        redisTemplate.opsForValue().set("exchange-uc:token:" + token, String.valueOf(userId), 30, TimeUnit.MINUTES);

        log.info("【用户登录成功】userId：{}，token：{}", userId, token);
        return "操作成功";
    }
}
