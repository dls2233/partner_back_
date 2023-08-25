package com.partner.boot.mapper;

import com.partner.boot.entity.Dynamic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 动态 Mapper 接口
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-19
 */
public interface DynamicMapper extends BaseMapper<Dynamic> {
    @Update("update dynamic set view = view + 1 where id = #{id}")
    void updateView(Integer id);
}
