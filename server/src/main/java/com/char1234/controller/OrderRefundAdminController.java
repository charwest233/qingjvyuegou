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
 * 售后 - 管理端接口
 */
@RestController
@RequestMapping("/api/admin/refund")
public class OrderRefundAdminController {

    @Autowired
    private OrderRefundService refundService;

    /** 查询售后列表 */
    @GetMapping("/list")
    public Result<List<OrderRefund>> list(@RequestParam(required = false) Integer status) {
        return Result.success(refundService.getAllRefunds(status));
    }

    /** 查询售后详情 */
    @GetMapping("/{id}")
    public Result<OrderRefund> detail(@PathVariable Long id) {
        OrderRefund refund = refundService.getRefundDetail(id);
        if (refund == null) return Result.error("售后单不存在");
        return Result.success(refund);
    }

    /** 审核售后 */
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody Map<String, Object> params) {
        Long refundId = Long.valueOf(params.get("refundId").toString());
        boolean approved = Boolean.TRUE.equals(params.get("approved"));
        String remark = (String) params.getOrDefault("remark", "");
        try {
            refundService.auditRefund(refundId, getCurrentAdminId(), approved, remark);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 确认收货（完成退款） */
    @PutMapping("/confirm-receipt/{id}")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        try {
            refundService.confirmReceipt(id, getCurrentAdminId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getCurrentAdminId() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isAdmin()) throw new RuntimeException("未登录");
        return ctx.principalId();
    }
}
