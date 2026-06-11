package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.CustomerServiceMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerServiceMessageMapper extends BaseMapper<CustomerServiceMessage> {

    @Select("SELECT * FROM t_customer_service_message WHERE session_id = #{sessionId} ORDER BY created_at")
    List<CustomerServiceMessage> findBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT DISTINCT session_id FROM t_customer_service_message ORDER BY (SELECT MAX(created_at) FROM t_customer_service_message m2 WHERE m2.session_id = t_customer_service_message.session_id) DESC LIMIT #{limit}")
    List<String> findRecentSessions(@Param("limit") int limit);
}
