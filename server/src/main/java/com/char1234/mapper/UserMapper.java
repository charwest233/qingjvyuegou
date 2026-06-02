package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM t_user WHERE openid = #{openid}")
    User selectByOpenid(@Param("openid") String openid);

    /**
     * 查询用户统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNew, " +
            "SUM(CASE WHEN create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as weekActive " +
            "FROM t_user")
    java.util.Map<String, Object> selectUserStatistics();
}
