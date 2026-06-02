package com.char1234.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.Order;
import com.char1234.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(orderService.getOrderStatistics());
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
