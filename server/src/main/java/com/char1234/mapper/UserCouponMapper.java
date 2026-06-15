package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /** 查询用户可用优惠券 */
    @Select("SELECT * FROM t_user_coupon WHERE user_id = #{userId} AND status = 0 AND expires_at > #{now} ORDER BY min_amount ASC")
    List<UserCoupon> findAvailable(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /** 标记过期优惠券 */
    @Update("UPDATE t_user_coupon SET status = 2 WHERE user_id = #{userId} AND status = 0 AND expires_at <= #{now}")
    int markExpired(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
