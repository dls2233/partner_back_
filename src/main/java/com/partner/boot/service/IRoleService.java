package com.partner.boot.service;

import com.partner.boot.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.partner.boot.entity.RolePermission;
import com.partner.boot.mapper.RolePermissionMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-13
 */
public interface IRoleService extends IService<Role> {

    void savePermission(Integer roleId, List<Integer> permissionIds);
}
