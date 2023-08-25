package com.partner.boot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("sys_role_permission")
@Data
public class RolePermission {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer roleId;
    private Integer permissionId;
}
