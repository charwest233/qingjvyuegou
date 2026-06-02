package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.UserAddress;
import com.char1234.mapper.UserAddressMapper;
import com.char1234.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

    @Override
    public List<UserAddress> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime));
    }

    @Override
    public UserAddress getDefaultOrFirst(Long userId) {
        UserAddress first = getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("LIMIT 1"));
        if (first != null) {
            return first;
        }
        return getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAddress(Long userId, UserAddress address) {
        address.setId(null);
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        boolean isFirst = count(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)) == 0;
        boolean wantDefault = address.getIsDefault() != null && address.getIsDefault() == 1;
        if (wantDefault || isFirst) {
            clearDefault(userId);
            address.setIsDefault(1);
        } else {
            if (address.getIsDefault() == null) {
                address.setIsDefault(0);
            }
        }
        save(address);
        return address.getId();
    }

    private void clearDefault(Long userId) {
        UserAddress wipe = new UserAddress();
        wipe.setIsDefault(0);
        update(wipe, new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, Long id, UserAddress incoming) {
        UserAddress existed = requireOwned(userId, id);
        incoming.setUserId(userId);
        incoming.setId(id);
        incoming.setCreateTime(existed.getCreateTime());
        if (incoming.getIsDefault() != null && incoming.getIsDefault() == 1) {
            clearDefault(userId);
            incoming.setIsDefault(1);
        }
        incoming.setUpdateTime(LocalDateTime.now());
        return updateById(incoming);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long id) {
        UserAddress a = requireOwned(userId, id);
        removeById(id);
        if (a.getIsDefault() != null && a.getIsDefault() == 1) {
            UserAddress next = getOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getCreateTime)
                    .last("LIMIT 1"));
            if (next != null) {
                next.setIsDefault(1);
                updateById(next);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long userId, Long id) {
        requireOwned(userId, id);
        clearDefault(userId);
        UserAddress patch = new UserAddress();
        patch.setId(id);
        patch.setIsDefault(1);
        patch.setUpdateTime(LocalDateTime.now());
        return updateById(patch);
    }

    @Override
    public UserAddress requireOwned(Long userId, Long id) {
        UserAddress a = getById(id);
        if (a == null || !a.getUserId().equals(userId)) {
            throw new IllegalArgumentException("收货地址不存在");
        }
        return a;
    }
}
