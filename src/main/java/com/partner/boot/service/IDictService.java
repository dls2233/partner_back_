package com.partner.boot.service;

import com.partner.boot.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-16
 */
public interface IDictService extends IService<Dict> {

    List<Dict> findIcons();
}
