package com.partner.boot.mapper;

import com.partner.boot.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dalaoshi
 * @since 2023-07-27
 */
public interface UserMapper extends BaseMapper<User> {
    int select1();

}
