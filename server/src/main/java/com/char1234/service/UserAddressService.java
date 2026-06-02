package com.char1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddress> listByUserId(Long userId);

    UserAddress getDefaultOrFirst(Long userId);

    Long saveAddress(Long userId, UserAddress address);

    boolean updateAddress(Long userId, Long id, UserAddress address);

    boolean deleteAddress(Long userId, Long id);

    boolean setDefault(Long userId, Long id);

    UserAddress requireOwned(Long userId, Long id);
}
