package com.partner.boot.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.partner.boot.entity.Role;
import com.partner.boot.entity.RolePermission;
import com.partner.boot.exception.ServiceException;
import com.partner.boot.mapper.RoleMapper;
import com.partner.boot.mapper.RolePermissionMapper;
import com.partner.boot.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.ConnectException;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-13
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private RolePermissionMapper rolePermissionMapper;
    @Override
    public void savePermission(Integer roleId, List<Integer> permissionIds) {
        if (CollUtil.isEmpty(permissionIds) || roleId==null){
            throw new ServiceException("数据不能为空");
        }
        rolePermissionMapper.delete(new UpdateWrapper<RolePermission>()
                .eq("role_id",roleId));
        permissionIds.forEach(v -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(v);
            rolePermissionMapper.insert(rolePermission);
        });
    }
}
