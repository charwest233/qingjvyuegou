package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper 接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
