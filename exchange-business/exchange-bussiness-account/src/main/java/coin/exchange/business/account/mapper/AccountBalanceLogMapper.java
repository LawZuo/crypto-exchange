package coin.exchange.business.account.mapper;

import coin.exchange.business.account.domain.AccountBalanceLogDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountBalanceLogMapper extends BaseMapper<AccountBalanceLogDo> {

    /**
     * 根据用户ID查询资金记录
     */
    @Select("select * from account_balance_log where user_id = #{userId} and is_deleted = 0 order by create_time desc")
    List<AccountBalanceLogDo> listByUserId(@Param("userId") Long userId);

    /**
     * 根据关联业务查询资金记录
     */
    @Select("select * from account_balance_log where association_type = #{associationType} and association_id = #{associationId} and is_deleted = 0")
    List<AccountBalanceLogDo> listByAssociation(@Param("associationType") Integer associationType,
                                                @Param("associationId") Long associationId);
}
