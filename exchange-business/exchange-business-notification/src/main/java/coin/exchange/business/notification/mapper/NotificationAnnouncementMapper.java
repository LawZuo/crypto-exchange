package coin.exchange.business.notification.mapper;

import coin.exchange.business.notification.domain.NotificationAnnouncementDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationAnnouncementMapper extends BaseMapper<NotificationAnnouncementDo> {

    @Select("""
            select * from notification_announcement
            where event_id = #{eventId} and is_deleted = 0
            limit 1
            """)
    NotificationAnnouncementDo getByEventId(@Param("eventId") String eventId);

    @Select("""
            select a.* from notification_announcement a
            where a.is_deleted = 0
              and (#{readStatus} is null
                   or (#{readStatus} = 1 and exists (
                        select 1 from notification_read r
                        where r.user_id = #{userId}
                          and r.target_type = 'ALL'
                          and r.target_id = a.id
                          and r.is_deleted = 0))
                   or (#{readStatus} = 0 and not exists (
                        select 1 from notification_read r
                        where r.user_id = #{userId}
                          and r.target_type = 'ALL'
                          and r.target_id = a.id
                          and r.is_deleted = 0)))
            order by a.create_time desc
            limit #{offset}, #{pageSize}
            """)
    List<NotificationAnnouncementDo> listForUser(@Param("userId") Long userId,
                                                 @Param("readStatus") Integer readStatus,
                                                 @Param("offset") int offset,
                                                 @Param("pageSize") int pageSize);

    @Select("""
            select count(1) from notification_announcement a
            where a.is_deleted = 0
              and not exists (
                    select 1 from notification_read r
                    where r.user_id = #{userId}
                      and r.target_type = 'ALL'
                      and r.target_id = a.id
                      and r.is_deleted = 0)
            """)
    long countUnread(@Param("userId") Long userId);
}
