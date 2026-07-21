package coin.exchange.module.user.controller;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.common.core.response.R;
import coin.exchange.module.user.domain.UserDo;
import coin.exchange.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public R<UserVo> getUser(
            @PathVariable("username") String username,
            HttpServletRequest request
    ) {
        log.info("用户信息接口被调用，参数：{}", username);
        try {
            UserDo user = userService.getUserByUsername(username);
            log.warn("查询到的用户信息：{}", user.toString());

            UserVo userVO = new UserVo();
            BeanUtil.copyProperties(user, userVO);
            log.warn("将返回的用户信息：{}", userVO);
            return R.success(userVO);
        } catch (Exception e) {
            log.error("调用用户信息接口报错, {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/register")
    public R<Long> registerUser(@RequestBody RegisterUserDto dto, HttpServletRequest request) {
        log.info("注册用户信息接口被调用，参数：{}", dto.toString());
        try {
            Long userId = userService.createUser(dto, request);
            log.warn("注册用户信息成功，返回用户ID：{}", userId);
            return R.success(userId);
        } catch (Exception e) {
            log.error("注册用户信息报错, {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }
}
