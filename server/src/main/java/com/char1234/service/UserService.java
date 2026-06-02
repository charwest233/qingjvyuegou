package com.char1234.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    /**
     * 根据 openid / unionid 查询或新建用户。
     */
    User getOrCreateByOpenid(String openid, String unionId);

    /**
     * 更新小程序用户昵称与头像。
     */
    boolean updateProfile(Long userId, String nickname, String avatarUrl);

    /**
     * 分页查询用户列表
     */
    Page<User> pageList(Integer page, Integer size, String nickname);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 注册普通用户（type=2）
     */
    User register(String phone, String password, String email);

    /**
     * 手机号/邮箱 + 密码登录
     */
    User login(String account, String password);

    /**
     * 更新手机号
     */
    boolean updatePhone(Long userId, String phone);

    /**
     * 更新邮箱
     */
    boolean updateEmail(Long userId, String email);
}
