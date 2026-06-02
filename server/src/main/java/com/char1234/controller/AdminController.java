package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.entity.Admin;
import com.char1234.service.AdminService;
import com.char1234.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 Controller
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }

        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return Result.error(401, "用户名或密码错误");
        }

        String token = JwtUtil.generateAdminToken(admin.getId(), admin.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminInfo", admin);

        return Result.success(result);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<Admin> info(@RequestHeader("Authorization") String token) {
        Long adminId = JwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        if (adminId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return Result.error(401, "管理员不存在");
        }

        return Result.success(admin);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, String> params) {
        Long adminId = JwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        if (adminId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("原密码和新密码不能为空");
        }

        boolean success = adminService.changePassword(adminId, oldPassword, newPassword);
        if (!success) {
            return Result.error("原密码错误");
        }

        return Result.success(true);
    }
}
