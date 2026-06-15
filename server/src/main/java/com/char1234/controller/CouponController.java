package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.UserCoupon;
import com.char1234.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /** 抽奖状态（今日次数、奖品列表） */
    @GetMapping("/draw-status")
    public Result<Map<String, Object>> drawStatus() {
        return Result.success(couponService.getDrawStatus(getUserId()));
    }

    /** 执行一次抽奖 */
    @PostMapping("/draw")
    public Result<UserCoupon> draw() {
        try {
            UserCoupon won = couponService.draw(getUserId());
            if (won == null) return Result.success(null);
            return Result.success(won);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 我的优惠券 */
    @GetMapping("/my")
    public Result<List<UserCoupon>> myCoupons() {
        return Result.success(couponService.getAvailableCoupons(getUserId()));
    }

    /** 获取可用于指定金额的优惠券 */
    @GetMapping("/available")
    public Result<List<UserCoupon>> available(@RequestParam BigDecimal amount) {
        List<UserCoupon> all = couponService.getAvailableCoupons(getUserId());
        all.removeIf(c -> amount.compareTo(c.getMinAmount()) < 0);
        return Result.success(all);
    }

    private Long getUserId() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) throw new RuntimeException("未登录");
        return ctx.principalId();
    }
}
