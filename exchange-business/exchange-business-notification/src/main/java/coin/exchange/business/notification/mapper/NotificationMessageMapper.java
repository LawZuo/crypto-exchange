package coin.exchange.business.notification.mapper;

import coin.exchange.business.notification.domain.NotificationMessageDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessageDo> {

    @Select("""
            select * from notification_message
            where event_id = #{eventId} and is_deleted = 0
            limit 1
            """)
    NotificationMessageDo getByEventId(@Param("eventId") String eventId);

    @Select("""
            select * from notification_message
            where user_id = #{userId}
              and is_deleted = 0
              and (#{readStatus} is null or read_status = #{readStatus})
            order by create_time desc
            limit #{offset}, #{pageSize}
            """)
    List<NotificationMessageDo> listByUser(@Param("userId") Long userId,
                                           @Param("readStatus") Integer readStatus,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    @Select("""
            select count(1) from notification_message
            where user_id = #{userId} and read_status = 0 and is_deleted = 0
            """)
    long countUnread(@Param("userId") Long userId);

    @Update("""
            update notification_message
            set read_status = 1, read_time = #{readTime}
            where id = #{id} and user_id = #{userId} and is_deleted = 0
            """)
    int markRead(@Param("userId") Long userId,
                 @Param("id") Long id,
                 @Param("readTime") LocalDateTime readTime);

    @Update("""
            update notification_message
            set read_status = 1, read_time = #{readTime}
            where user_id = #{userId} and read_status = 0 and is_deleted = 0
            """)
    int markAllRead(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);
}
