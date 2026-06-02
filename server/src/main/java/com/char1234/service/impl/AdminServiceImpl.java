package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Admin;
import com.char1234.mapper.AdminMapper;
import com.char1234.service.AdminService;
import cn.hutool.crypto.digest.MD5;
import org.springframework.stereotype.Service;

/**
 * 管理员 Service 实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin login(String username, String password) {
        // MD5加密密码
        String md5Password = MD5.create().digestHex(password);
        
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username)
               .eq(Admin::getPassword, md5Password);
        
        return getOne(wrapper);
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        Admin admin = getById(id);
        if (admin == null) {
            return false;
        }
        
        // 验证原密码
        String oldMd5 = MD5.create().digestHex(oldPassword);
        if (!oldMd5.equals(admin.getPassword())) {
            return false;
        }
        
        // 更新新密码
        String newMd5 = MD5.create().digestHex(newPassword);
        admin.setPassword(newMd5);
        return updateById(admin);
    }
}
