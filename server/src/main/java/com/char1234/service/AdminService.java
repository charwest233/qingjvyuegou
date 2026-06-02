package com.char1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.Admin;

/**
 * 管理员 Service 接口
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码(MD5)
     * @return 登录成功返回管理员信息，失败返回null
     */
    Admin login(String username, String password);

    /**
     * 修改密码
     * @param id 管理员ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
}
