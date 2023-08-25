package com.partner.boot.service.impl;

import com.partner.boot.entity.Dynamic;
import com.partner.boot.mapper.DynamicMapper;
import com.partner.boot.service.IDynamicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 动态 服务实现类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-19
 */
@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements IDynamicService {

    @Resource
    DynamicMapper dynamicMapper;
    //更新浏览量加一
    @Override
    public void updateView(Integer id) {
        dynamicMapper.updateView(id);
    }
}
