package com.partner.boot.service.impl;

import com.partner.boot.entity.Im;
import com.partner.boot.mapper.ImMapper;
import com.partner.boot.service.IImService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-18
 */
@Service
public class ImServiceImpl extends ServiceImpl<ImMapper, Im> implements IImService {

}
