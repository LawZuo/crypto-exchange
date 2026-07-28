package coin.exchange.module.user.mapper;

import coin.exchange.module.user.domain.UserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<UserDo> {

    /**
     * 根据uid获取用户信息
     * @param uid 用户uid
     * @return 用户信息
     */
    @Select("select * from users where uid = #{uid} and is_deleted = 0")
    UserDo getUserByUid(@Param("uid") String uid);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Select("select * from users where username = #{username} and is_deleted = 0")
    UserDo getUserByUsername(@Param("username") String username);

    /**
     * 根据邮箱获取用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("select * from users where email = #{email} and is_deleted = 0")
    UserDo getUserByEmail(@Param("email") String email);

    /**
     * 创建用户 - 登录时创建
     */
    @Insert("insert into users (username, email, password, uid) values (#{username}, #{email}, #{password}, #{uid})")
    void createUser(@Param("username") String username, @Param("email") String email, @Param("password") String password, @Param("uid") String uid);

    /**
     * 更新用户最近登录信息
     */
    @Update("update users set last_login_ip = #{loginIp}, last_login_time = now() where id = #{userId} and is_deleted = 0")
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);
}
