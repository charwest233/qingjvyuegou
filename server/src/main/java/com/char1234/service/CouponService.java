package com.char1234.service;

import com.char1234.entity.Coupon;
import com.char1234.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {

    /** 获取今日抽奖状态 */
    Map<String, Object> getDrawStatus(Long userId);

    /** 执行一次抽奖，返回中奖券或null */
    UserCoupon draw(Long userId);

    /** 获取用户可用优惠券 */
    List<UserCoupon> getAvailableCoupons(Long userId);

    /** 使用优惠券 */
    void useCoupon(Long userCouponId, Long userId, Long orderId);

    /** 计算优惠后金额 */
    BigDecimal calcDiscount(BigDecimal amount, UserCoupon coupon);
}
