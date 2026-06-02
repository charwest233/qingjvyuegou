package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.CartItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemEntity> {
}
