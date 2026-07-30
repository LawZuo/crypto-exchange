package coin.exchange.module.user.mapper;

import coin.exchange.module.user.domain.UserLoginLogDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLogDo> {
}
