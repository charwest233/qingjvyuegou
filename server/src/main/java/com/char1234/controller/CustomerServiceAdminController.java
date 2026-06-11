package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.entity.CustomerServiceMessage;
import com.char1234.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人工客服管理端接口
 */
@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceAdminController {

    @Autowired
    private CustomerServiceService csService;

    /** 获取最近活跃会话 */
    @GetMapping("/sessions")
    public Result<List<String>> getSessions(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(csService.getRecentSessions(limit));
    }

    /** 获取指定会话的消息历史 */
    @GetMapping("/messages/{sessionId}")
    public Result<List<CustomerServiceMessage>> getMessages(@PathVariable String sessionId) {
        return Result.success(csService.getSessionMessages(sessionId));
    }
}
