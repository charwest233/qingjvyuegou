package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Coupon;
import com.char1234.entity.CouponDraw;
import com.char1234.entity.UserCoupon;
import com.char1234.mapper.CouponDrawMapper;
import com.char1234.mapper.CouponMapper;
import com.char1234.mapper.UserCouponMapper;
import com.char1234.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponServiceImpl
        extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements CouponService {

    /** 每日最多抽奖次数 */
    private static final int MAX_DAILY_DRAWS = 10;
    /** 中奖综合概率 */
    private static final double WIN_RATE = 0.5;

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private CouponDrawMapper couponDrawMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;

    private final Random random = new Random();

    @Override
    public Map<String, Object> getDrawStatus(Long userId) {
        LocalDate today = LocalDate.now();
        CouponDraw record = couponDrawMapper.findByUserAndDate(userId, today);
        int count = record == null ? 0 : record.getDrawCount();

        // 标记过期
        userCouponMapper.markExpired(userId, LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("todayDraws", count);
        result.put("maxDraws", MAX_DAILY_DRAWS);
        result.put("remaining", MAX_DAILY_DRAWS - count);

        List<Coupon> allCoupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1));
        result.put("prizes", allCoupons);

        return result;
    }

    @Override
    @Transactional
    public UserCoupon draw(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 获取/创建今日抽奖记录
        CouponDraw record = couponDrawMapper.findByUserAndDate(userId, today);
        if (record == null) {
            record = new CouponDraw();
            record.setUserId(userId);
            record.setDrawDate(today);
            record.setDrawCount(0);
            couponDrawMapper.insert(record);
        }
        if (record.getDrawCount() >= MAX_DAILY_DRAWS) {
            throw new RuntimeException("今日抽奖次数已用完");
        }

        // 2. 更新已抽次数
        record.setDrawCount(record.getDrawCount() + 1);
        couponDrawMapper.updateById(record);

        // 3. 判断是否中奖（50%）
        if (random.nextDouble() >= WIN_RATE) {
            log.info("用户 {} 第{}次抽奖未中奖", userId, record.getDrawCount());
            return null;
        }

        // 4. 按权重随机选择优惠券
        List<Coupon> activeCoupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .orderByAsc(Coupon::getTier));
        if (activeCoupons.isEmpty()) return null;

        Coupon won = weightedRandom(activeCoupons);
        if (won == null) return null;

        // 5. 发放优惠券
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(won.getId());
        uc.setCouponNo(generateCouponNo(userId));
        uc.setName(won.getName());
        uc.setType(won.getType());
        uc.setValue(won.getValue());
        uc.setMinAmount(won.getMinAmount());
        uc.setStatus(0);
        uc.setExpiresAt(LocalDateTime.now().plusDays(won.getValidDays()));
        save(uc);

        log.info("用户 {} 第{}次抽奖中奖: {} (tier={})", userId, record.getDrawCount(), won.getName(), won.getTier());
        return uc;
    }

    @Override
    public List<UserCoupon> getAvailableCoupons(Long userId) {
        userCouponMapper.markExpired(userId, LocalDateTime.now());
        return userCouponMapper.findAvailable(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void useCoupon(Long userCouponId, Long userId, Long orderId) {
        UserCoupon uc = getById(userCouponId);
        if (uc == null) throw new RuntimeException("优惠券不存在");
        if (!uc.getUserId().equals(userId)) throw new RuntimeException("无权使用");
        if (uc.getStatus() != 0) throw new RuntimeException("优惠券已使用或已过期");
        if (uc.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("优惠券已过期");

        uc.setStatus(1);
        uc.setOrderId(orderId);
        uc.setUsedAt(LocalDateTime.now());
        updateById(uc);
    }

    @Override
    public BigDecimal calcDiscount(BigDecimal amount, UserCoupon coupon) {
        if (coupon == null) return BigDecimal.ZERO;
        if (amount.compareTo(coupon.getMinAmount()) < 0) return BigDecimal.ZERO;
        if (coupon.getType() == 2) {
            // 折扣券：value=0.9 表示9折
            return amount.multiply(BigDecimal.ONE.subtract(coupon.getValue()))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 满减券
        return coupon.getValue().min(amount);
    }

    // ===== 工具方法 =====

    private Coupon weightedRandom(List<Coupon> coupons) {
        int totalWeight = coupons.stream().mapToInt(Coupon::getWeight).sum();
        int hit = random.nextInt(totalWeight);
        int accum = 0;
        for (Coupon c : coupons) {
            accum += c.getWeight();
            if (hit < accum) return c;
        }
        return coupons.get(coupons.size() - 1);
    }

    private String generateCouponNo(Long userId) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = random.nextInt(9000) + 1000;
        return "CP" + ts + rand;
    }
}
