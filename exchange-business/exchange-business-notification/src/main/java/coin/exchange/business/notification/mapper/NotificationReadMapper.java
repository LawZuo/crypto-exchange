package coin.exchange.business.notification.mapper;

import coin.exchange.business.notification.domain.NotificationReadDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Set;

@Mapper
public interface NotificationReadMapper extends BaseMapper<NotificationReadDo> {

    @Select("""
            select count(1) from notification_read
            where user_id = #{userId}
              and target_type = #{targetType}
              and target_id = #{targetId}
              and is_deleted = 0
            """)
    int existsRead(@Param("userId") Long userId,
                   @Param("targetType") String targetType,
                   @Param("targetId") Long targetId);

    @Insert("""
            insert ignore into notification_read(user_id, target_type, target_id, read_time)
            values(#{userId}, #{targetType}, #{targetId}, #{readTime})
            """)
    int insertIgnore(@Param("userId") Long userId,
                     @Param("targetType") String targetType,
                     @Param("targetId") Long targetId,
                     @Param("readTime") LocalDateTime readTime);

    @Select("""
            select target_id from notification_read
            where user_id = #{userId}
              and target_type = #{targetType}
              and is_deleted = 0
            """)
    Set<Long> listReadTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType);
}
