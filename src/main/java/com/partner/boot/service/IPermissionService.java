package com.partner.boot.service;

import com.partner.boot.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-13
 */
public interface IPermissionService extends IService<Permission> {

    List<Permission> tree();
}
