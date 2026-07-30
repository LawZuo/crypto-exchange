package coin.exchange.module.user.mapper;

import coin.exchange.module.user.domain.KycApplicationDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KycApplicationMapper extends BaseMapper<KycApplicationDo> {

    /**
     * 根据userId查询用户KYC申请
     */
    @Select("select * from user_kyc_application where user_id = #{userId}")
    KycApplicationDo getKycApplication(@Param("userId") Long userId);
}
