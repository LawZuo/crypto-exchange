package coin.exchange.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import coin.exchange.api.user.dto.RegisterUserDto;
import coin.exchange.api.user.model.UserVo;
import coin.exchange.common.core.utils.IpUtil;
import coin.exchange.common.core.utils.UUIDUtil;
import coin.exchange.module.user.domain.UserDo;
import coin.exchange.module.user.mapper.UserMapper;
import coin.exchange.module.user.service.UserService;
import coin.exchange.module.user.utils.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDo getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 查询用户
        return userMapper.selectById(userId);
    }

    @Override
    public UserVo getUserByUid(String uid) {
        if (Objects.isNull(uid) || uid.isEmpty()) {
            throw new IllegalArgumentException("UID不能为空");
        }
        // 查询用户
        UserDo user = userMapper.getUserByUsername(uid);
        UserVo userVO = new UserVo();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserDo getUserByUsername(String username) {
        if (Objects.isNull(username) || username.isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        return userMapper.getUserByUsername(username);
    }

    @Override
    public UserVo getUserByEmail(String email) {
        if (Objects.isNull(email) || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        // 查询用户
        UserDo user = userMapper.getUserByUsername(email);
        UserVo userVO = new UserVo();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public Long createUser(RegisterUserDto dto, HttpServletRequest request) {
        ValidationUtil.validateUser(dto);

        // 邮箱&账号不能重复
        if (userMapper.getUserByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("账号已存在");
        }
        if (userMapper.getUserByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        // TODO 密码加密 使用Spring Security 标准

        // 记录注册IP地址
        String clientIp = IpUtil.getClientIp(request);

        // 生成UID
        String uid = UUIDUtil.generate8DigitId();

        // 创建用户
        UserDo userDo = new UserDo();
        BeanUtil.copyProperties(dto, userDo);
        userDo.setUid(uid);
        userDo.setRegisterIp(clientIp);
        userDo.setStatus(1);
        log.info("注册用户：{}", userDo);

        Long userId = (long) userMapper.insert(userDo);
        return userId;
    }
}
