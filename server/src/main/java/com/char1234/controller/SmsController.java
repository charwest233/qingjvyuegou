package com.char1234.controller;

import cn.hutool.core.util.RandomUtil;
import com.char1234.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码 Controller
 * 验证码存 Redis，5 分钟自动过期
 */
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final long SMS_CODE_TTL = 300; // 5分钟

    private final StringRedisTemplate redisTemplate;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody SendCodeRequest request) {
        String phone = request.getPhone();
        if (phone == null || phone.length() < 11) {
            return Result.error("请输入正确的手机号");
        }

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis，5分钟过期
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, SMS_CODE_TTL, TimeUnit.SECONDS);

        // 开发环境直接返回验证码，方便调试
        return Result.success(code);
    }

    /**
     * 校验验证码
     */
    @PostMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestBody VerifyCodeRequest request) {
        boolean valid = checkCode(request.getPhone(), request.getCode());
        if (valid) {
            // 验证通过后删除key
            redisTemplate.delete(SMS_CODE_PREFIX + request.getPhone());
        }
        return valid ? Result.success(true) : Result.error("验证码错误或已过期");
    }

    /**
     * 内部校验
     */
    public boolean checkCode(String phone, String code) {
        if (phone == null || code == null) return false;
        String stored = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        return code.equals(stored);
    }

    // --- 请求体 ---

    @lombok.Data
    public static class SendCodeRequest {
        private String phone;
    }

    @lombok.Data
    public static class VerifyCodeRequest {
        private String phone;
        private String code;
    }
}
