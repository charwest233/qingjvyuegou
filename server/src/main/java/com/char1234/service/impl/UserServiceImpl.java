package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.User;
import com.char1234.mapper.UserMapper;
import com.char1234.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户 Service 实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getOrCreateByOpenid(String openid, String unionId) {
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionId(unionId);
            user.setNickname("微信用户");
            user.setType(2);
            user.setCreateTime(LocalDateTime.now());
            save(user);
            return user;
        }
        if (StringUtils.isNotBlank(unionId) && StringUtils.isBlank(user.getUnionId())) {
            user.setUnionId(unionId);
            updateById(user);
        }
        return user;
    }

    @Override
    public boolean updateProfile(Long userId, String nickname, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        return updateById(user);
    }

    @Override
    public Page<User> pageList(Integer page, Integer size, String nickname) {
        Page<User> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        wrapper.orderByDesc(User::getCreateTime);

        return page(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics() {
        return userMapper.selectUserStatistics();
    }

    @Override
    public User register(String phone, String password, String email) {
        if (StringUtils.isBlank(phone)) {
            throw new RuntimeException("手机号不能为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("密码不能为空");
        }

        // 检查手机号是否已注册
        User exist = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone), false);
        if (exist != null) {
            throw new RuntimeException("该手机号已注册");
        }

        // 检查邮箱是否已注册
        if (StringUtils.isNotBlank(email)) {
            User emailExist = getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email), false);
            if (emailExist != null) {
                throw new RuntimeException("该邮箱已注册");
            }
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setEmail(email);
        user.setType(2);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setCreateTime(LocalDateTime.now());
        save(user);
        return user;
    }

    @Override
    public boolean updatePhone(Long userId, String phone) {
        if (StringUtils.isBlank(phone)) {
            throw new RuntimeException("手机号不能为空");
        }
        // 检查手机号是否已被其他用户使用
        User exist = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .ne(User::getId, userId), false);
        if (exist != null) {
            throw new RuntimeException("该手机号已被绑定");
        }
        User user = getById(userId);
        if (user == null) return false;
        user.setPhone(phone);
        return updateById(user);
    }

    @Override
    public boolean updateEmail(Long userId, String email) {
        if (StringUtils.isBlank(email)) {
            throw new RuntimeException("邮箱不能为空");
        }
        // 检查邮箱是否已被其他用户使用
        User exist = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .ne(User::getId, userId), false);
        if (exist != null) {
            throw new RuntimeException("该邮箱已被绑定");
        }
        User user = getById(userId);
        if (user == null) return false;
        user.setEmail(email);
        return updateById(user);
    }

    @Override
    public User login(String account, String password) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            throw new RuntimeException("账号或密码不能为空");
        }

        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, account)
                .or()
                .eq(User::getEmail, account), false);

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }

        String md5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}
