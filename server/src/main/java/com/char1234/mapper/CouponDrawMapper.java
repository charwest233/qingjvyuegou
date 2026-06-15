package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.CouponDraw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface CouponDrawMapper extends BaseMapper<CouponDraw> {

    @Select("SELECT * FROM t_coupon_draw WHERE user_id = #{userId} AND draw_date = #{date}")
    CouponDraw findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
