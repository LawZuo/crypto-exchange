package coin.exchange.module.user.service;

import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.module.user.domain.UserDo;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    /**
     * 根据userId获取用户信息
     */
    UserDo getUser(Long userId);

    /**
     * 根据uid获取用户信息
     */
    UserVo getUserByUid(String uid);

    /**
     * 根据username获取用户信息
     */
    UserDo getUserByUsername(String username);

    /**
     * 根据email获取用户信息
     */
    UserVo getUserByEmail(String email);

    /**
     * 创建用户, 返回userId
     */
    Long createUser(RegisterUserDto user, HttpServletRequest request);
}
