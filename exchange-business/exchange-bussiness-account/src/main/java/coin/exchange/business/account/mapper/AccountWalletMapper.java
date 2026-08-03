package coin.exchange.business.account.mapper;

import coin.exchange.business.account.domain.AccountWalletDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountWalletMapper extends BaseMapper<AccountWalletDo> {

    /**
     * 根据用户ID查询钱包列表
     */
    @Select("select * from account_wallet where user_id = #{userId} and is_deleted = 0")
    List<AccountWalletDo> listByUserId(@Param("userId") Long userId);

    /**
     * 根据用户、币种和钱包类型查询钱包
     */
    @Select("select * from account_wallet where user_id = #{userId} and currency = #{currency} and wallet_type = #{walletType} and is_deleted = 0")
    AccountWalletDo getByUserCurrencyType(@Param("userId") Long userId,
                                          @Param("currency") String currency,
                                          @Param("walletType") Integer walletType);
}
