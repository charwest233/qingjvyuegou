package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.UserAddress;
import com.char1234.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序收货地址 CRUD（注意：具体路径须在 /{id} 之前声明，例如 /default）。
 */
@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressMpController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/default")
    public Result<UserAddress> getDefault() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        UserAddress ua = userAddressService.getDefaultOrFirst(ctx.principalId());
        return Result.success(ua);
    }

    @GetMapping
    public Result<List<UserAddress>> list() {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        return Result.success(userAddressService.listByUserId(ctx.principalId()));
    }

    @GetMapping("/{id}")
    public Result<UserAddress> getOne(@PathVariable Long id) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        try {
            return Result.success(userAddressService.requireOwned(ctx.principalId(), id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<Long> create(@RequestBody UserAddress body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        try {
            Long id = userAddressService.saveAddress(ctx.principalId(), body);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserAddress body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        try {
            return Result.success(userAddressService.updateAddress(ctx.principalId(), id, body));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/default")
    public Result<Boolean> setDefault(@PathVariable Long id) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        try {
            return Result.success(userAddressService.setDefault(ctx.principalId(), id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        try {
            return Result.success(userAddressService.deleteAddress(ctx.principalId(), id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
