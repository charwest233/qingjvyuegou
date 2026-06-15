package com.char1234.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.Order;
import com.char1234.entity.TrackingDTO;
import com.char1234.entity.UserCoupon;
import com.char1234.mapper.UserCouponMapper;
import com.char1234.service.CouponService;
import com.char1234.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单 Controller（管理端全部能力 + 小程序用户下单/模拟支付）。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        JwtContextHolder.Context ctx = JwtContextHolder.get();
        Long effectiveUserId = userId;
        if (ctx != null && ctx.isMpUser()) {
            effectiveUserId = ctx.principalId();
        }

        Page<Order> pageResult = orderService.pageList(page, size, orderNo, status, effectiveUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (!canAccessOrder(order, ctx)) {
            return Result.error(403, "无权限查看该订单");
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<Order> create(@RequestBody Map<String, Object> params) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可创建订单");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
        Long addressId = null;
        Object aid = params.get("addressId");
        if (aid != null && !aid.toString().isEmpty()) {
            addressId = Long.valueOf(aid.toString());
        }
        String receiverName = params.get("receiverName") != null ? params.get("receiverName").toString() : null;
        String receiverPhone = params.get("receiverPhone") != null ? params.get("receiverPhone").toString() : null;
        String receiverAddress = params.get("receiverAddress") != null ? params.get("receiverAddress").toString() : null;

        try {
            Order order = orderService.createOrder(ctx.principalId(), items,
                    addressId, receiverName, receiverPhone, receiverAddress);
            // 使用优惠券
            Object cid = params.get("couponId");
            if (cid != null) {
                Long couponId = Long.valueOf(cid.toString());
                couponService.useCoupon(couponId, ctx.principalId(), order.getId());
                // 更新订单实付金额（原价 - 优惠券抵扣）
                UserCoupon uc = userCouponMapper.selectById(couponId);
                if (uc != null) {
                    BigDecimal discount = couponService.calcDiscount(order.getTotalAmount(), uc);
                    order.setTotalAmount(order.getTotalAmount().subtract(discount));
                    orderService.updateById(order);
                }
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/pay")
    public Result<Boolean> pay(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!canAccessOrder(order, JwtContextHolder.get())) {
            return Result.error(403, "无权限支付该订单");
        }
        boolean success = orderService.payOrder(id);
        if (!success) {
            return Result.error("支付失败");
        }
        return Result.success(true);
    }

    @PutMapping("/{id}/ship")
    public Result<Boolean> ship(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String company = params.get("company");
        String trackingNo = params.get("trackingNo");

        boolean success = orderService.shipOrder(id, company, trackingNo);
        if (!success) {
            return Result.error("发货失败");
        }
        return Result.success(true);
    }

    /**
     * 管理端强行设置订单状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("缺少 status");
        }
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(status);
        return Result.success(orderService.updateById(order));
    }

    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancel(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!canAccessOrder(order, JwtContextHolder.get())) {
            return Result.error(403, "无权限取消该订单");
        }
        boolean success = orderService.cancelOrder(id);
        if (!success) {
            return Result.error("取消失败");
        }
        return Result.success(true);
    }

    @PutMapping("/{id}/confirm")
    public Result<Boolean> confirm(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!canAccessOrder(order, JwtContextHolder.get())) {
            return Result.error(403, "无权限确认收货");
        }
        boolean success = orderService.confirmOrder(id);
        if (!success) {
            return Result.error("确认收货失败");
        }
        return Result.success(true);
    }

    /**
     * 用户删除已完成/已取消的订单
     * DELETE /api/order/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可删除订单");
        }
        boolean success = orderService.deleteOrder(ctx.principalId(), id);
        if (!success) {
            return Result.error("删除失败，仅可删除已完成或已取消的订单");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}/tracking")
    public Result<TrackingDTO> tracking(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (!canAccessOrder(order, ctx)) return Result.error(403, "无权限查看");

        if (order.getStatus() < 2) return Result.error("订单尚未发货");

        return Result.success(generateSimulatedTracking(order));
    }

    /**
     * 根据收货地址模拟生成物流轨迹（演示用，非真实物流数据）
     */
    private TrackingDTO generateSimulatedTracking(Order order) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String now = LocalDateTime.now().format(fmt);

        // 根据收货地址解析城市坐标
        String addr = order.getReceiverAddress() != null ? order.getReceiverAddress() : "";
        double[] dst = guessCityCenter(addr);

        // 仓库坐标（当前城市偏郊区位置）
        double[] warehouse = new double[]{dst[0] - 0.25, dst[1] + 0.08};

        // 分拣中心坐标
        double[] sorting = new double[]{dst[0] - 0.10, dst[1] + 0.04};

        // 配送站坐标
        double[] station = new double[]{dst[0] - 0.03, dst[1] + 0.02};

        List<TrackingDTO.Point> route = new ArrayList<>();
        route.add(new TrackingDTO.Point(BigDecimal.valueOf(warehouse[0]), BigDecimal.valueOf(warehouse[1])));
        route.add(new TrackingDTO.Point(BigDecimal.valueOf(sorting[0]), BigDecimal.valueOf(sorting[1])));
        route.add(new TrackingDTO.Point(BigDecimal.valueOf(station[0]), BigDecimal.valueOf(station[1])));
        route.add(new TrackingDTO.Point(BigDecimal.valueOf(dst[0]), BigDecimal.valueOf(dst[1])));

        List<TrackingDTO.TrackingStep> steps = new ArrayList<>();
        steps.add(new TrackingDTO.TrackingStep(now, "包裹已出库，等待运输", BigDecimal.valueOf(warehouse[0]), BigDecimal.valueOf(warehouse[1])));
        steps.add(new TrackingDTO.TrackingStep(now, "已到达分拣中心", BigDecimal.valueOf(sorting[0]), BigDecimal.valueOf(sorting[1])));
        steps.add(new TrackingDTO.TrackingStep(now, "快递员正在派送", BigDecimal.valueOf(station[0]), BigDecimal.valueOf(station[1])));
        steps.add(new TrackingDTO.TrackingStep(now, "已签收", BigDecimal.valueOf(dst[0]), BigDecimal.valueOf(dst[1])));

        return new TrackingDTO(order.getId(), order.getOrderNo(),
                order.getExpressCompany(), order.getExpressNo(),
                order.getStatus() == 3 ? "delivered" : "shipping",
                order.getReceiverAddress(),
                route, steps);
    }

    /** 简易城市坐标映射（演示用） */
    private static double[] guessCityCenter(String address) {
        if (address.contains("北京")) return new double[]{116.397, 39.908};
        if (address.contains("上海")) return new double[]{121.474, 31.230};
        if (address.contains("广州")) return new double[]{113.264, 23.129};
        if (address.contains("深圳")) return new double[]{114.057, 22.543};
        if (address.contains("杭州")) return new double[]{120.155, 30.274};
        if (address.contains("成都")) return new double[]{104.066, 30.572};
        if (address.contains("武汉")) return new double[]{114.305, 30.593};
        if (address.contains("南京")) return new double[]{118.796, 32.060};
        if (address.contains("重庆")) return new double[]{106.551, 29.563};
        if (address.contains("西安")) return new double[]{108.940, 34.261};
        // 默认上海
        return new double[]{121.474, 31.230};
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(orderService.getOrderStatistics());
    }

    /**
     * 客服查询订单（根据订单号模糊搜索）
     */
    @GetMapping("/lookup")
    public Result<Order> lookup(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("请输入订单号");
        }
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(Order::getOrderNo, keyword.trim())
               .last("LIMIT 1");
        Order order = orderService.getOne(wrapper);
        if (order == null) {
            return Result.error("未找到相关订单");
        }
        return Result.success(order);
    }

    /**
     * 获取当前登录用户的各状态订单数量（用于个人中心红点）
     */
    @GetMapping("/my-statistics")
    public Result<Map<String, Object>> myStatistics() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "未登录");
        }
        Long userId = ctx.principalId();
        // 统计时排除已有售后记录的订单，避免退款完成后红点还在
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).eq(Order::getStatus, 0)));
        stats.put("paid", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).eq(Order::getStatus, 1)
                .notInSql(Order::getId, "SELECT order_id FROM t_order_refund WHERE status IN (0,1,2,3,4)")));
        stats.put("shipped", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).eq(Order::getStatus, 2)
                .notInSql(Order::getId, "SELECT order_id FROM t_order_refund WHERE status IN (0,1,2,3,4)")));
        stats.put("completed", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).in(Order::getStatus, 3, 4)));
        return Result.success(stats);
    }

    private static boolean canAccessOrder(Order order, JwtContextHolder.Context ctx) {
        if (ctx == null) {
            return false;
        }
        if (ctx.isAdmin()) {
            return true;
        }
        if (ctx.isMpUser()) {
            return order.getUserId().equals(ctx.principalId());
        }
        return false;
    }
}
