package com.char1234.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.User;
import com.char1234.service.UserService;
import com.char1234.service.WechatMiniappCodeService;
import com.char1234.service.WechatMiniappCodeService.WxSessionPayload;
import com.char1234.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户 Controller（管理端列表 + 小程序登录与资料）。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private WechatMiniappCodeService wechatMiniappCodeService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String nickname) {

        Page<User> pageResult = userService.pageList(page, size, nickname);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());

        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        return Result.success(true);
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = userService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 微信小程序登录（官方 jscode2session / 未配置密钥时 mock）
     */
    @PostMapping("/wx-login")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }
        WxSessionPayload session;
        try {
            session = wechatMiniappCodeService.exchange(code);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

        User user = userService.getOrCreateByOpenid(session.openid(), session.unionId());
        String token = JwtUtil.generateMpUserToken(user.getId(),
                user.getNickname() != null ? user.getNickname() : user.getOpenid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        return Result.success(result);
    }

    /**
     * 手机号/邮箱 + 密码 登录前台
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String password = params.get("password");
        try {
            User user = userService.login(account, password);
            String token = JwtUtil.generateMpUserToken(user.getId(),
                    user.getNickname() != null ? user.getNickname() : user.getPhone());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", user);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 注册普通用户（type=2），需短信验证码
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        String email = params.get("email");
        String code = params.get("code");

        // 校验验证码
        if (code == null || code.isEmpty()) {
            return Result.error("验证码不能为空");
        }
        String redisKey = "sms:code:" + phone;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        if (storedCode == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }
        // 验证通过后删除验证码
        redisTemplate.delete(redisKey);

        try {
            User user = userService.register(phone, password, email);
            // 注册完成后自动登录
            String token = JwtUtil.generateMpUserToken(user.getId(),
                    user.getNickname() != null ? user.getNickname() : user.getPhone());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", user);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新当前小程序用户信息
     */
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@RequestBody Map<String, String> body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可操作");
        }
        boolean ok = userService.updateProfile(
                ctx.principalId(),
                body.get("nickname"),
                body.get("avatarUrl"));
        return ok ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 修改手机号
     */
    @PutMapping("/phone")
    public Result<Boolean> updatePhone(@RequestBody Map<String, String> body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可操作");
        }
        try {
            boolean ok = userService.updatePhone(ctx.principalId(), body.get("phone"));
            return ok ? Result.success(true) : Result.error("修改失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改邮箱
     */
    @PutMapping("/email")
    public Result<Boolean> updateEmail(@RequestBody Map<String, String> body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可操作");
        }
        try {
            boolean ok = userService.updateEmail(ctx.principalId(), body.get("email"));
            return ok ? Result.success(true) : Result.error("修改失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
