package com.partner.boot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.partner.boot.common.LDTConfig;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.startup.Tomcat;

/**
* <p>
* 
* </p>
*
* @author dalaoshi
* @since 2023-08-13
*/
@Getter
@Setter
@TableName("sys_permission")
@ApiModel(value = "Permission对象", description = "")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 路径
    @ApiModelProperty("路径")
    @Alias("路径")
    private String path;

    // 顺序
    @ApiModelProperty("顺序")
    @Alias("顺序")
    private Integer orders;

    // 图标
    @ApiModelProperty("图标")
    @Alias("图标")
    private String icon;

    // 页面路径
    @ApiModelProperty("页面路径")
    @Alias("页面路径")
    private String page;

    // 父级id
    @ApiModelProperty("父级id")
    @Alias("父级id")
    private Integer pid;

    // 权限
    @ApiModelProperty("权限")
    @Alias("权限")
    private String auth;

    //类型
    @ApiModelProperty("类型")
    @Alias("类型")
    private Integer type;


    // 逻辑删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    // 更新时间
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<Permission> children;
    @ApiModelProperty("是否隐藏")
    @Alias("是否隐藏")
    private Boolean hide;
}