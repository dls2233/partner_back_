package com.partner.boot.service;

import com.partner.boot.entity.Dynamic;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 动态 服务类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-19
 */
public interface IDynamicService extends IService<Dynamic> {

    void updateView(Integer id);
}
