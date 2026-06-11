package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.OrderRefund;
import com.char1234.service.OrderRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 售后 - 用户端接口
 */
@RestController
@RequestMapping("/api/refund")
public class OrderRefundController {

    @Autowired
    private OrderRefundService refundService;

    /** 申请售后 */
    @PostMapping("/apply")
    public Result<OrderRefund> apply(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long orderItemId = params.get("orderItemId") != null
                ? Long.valueOf(params.get("orderItemId").toString()) : null;
        Integer type = Integer.valueOf(params.get("type").toString());
        String reason = (String) params.getOrDefault("reason", "");
        String description = (String) params.getOrDefault("description", "");

        try {
            OrderRefund refund = refundService.applyRefund(userId, orderId, orderItemId, type, reason, description);
            return Result.success(refund);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查询我的售后列表 */
    @GetMapping("/list")
    public Result<List<OrderRefund>> list() {
        return Result.success(refundService.getUserRefunds(getCurrentUserId()));
    }

    /** 查询售后详情 */
    @GetMapping("/{id}")
    public Result<OrderRefund> detail(@PathVariable Long id) {
        OrderRefund refund = refundService.getRefundDetail(id);
        if (refund == null) return Result.error("售后单不存在");
        if (!refund.getUserId().equals(getCurrentUserId())) return Result.error("无权查看");
        return Result.success(refund);
    }

    /** 填写退货物流 */
    @PutMapping("/return-logistics")
    public Result<Void> fillLogistics(@RequestBody Map<String, Object> params) {
        Long refundId = Long.valueOf(params.get("refundId").toString());
        String company = (String) params.get("expressCompany");
        String trackingNo = (String) params.get("expressNo");
        try {
            refundService.fillReturnLogistics(refundId, getCurrentUserId(), company, trackingNo);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 撤销申请 */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            refundService.cancelRefund(id, getCurrentUserId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) throw new RuntimeException("未登录");
        return ctx.principalId();
    }
}
