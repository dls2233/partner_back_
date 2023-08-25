package com.partner.boot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.partner.boot.common.LDTConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

/**
* <p>
* 
* </p>
*
* @author dalaoshi
* @since 2023-07-27
*/
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    @Alias("用户名")
    private String username;

    @ApiModelProperty("密码")
    @Alias("密码")
    private String password;

    @ApiModelProperty("昵称")
    @Alias("昵称")
    private String name;

    @ApiModelProperty("邮箱")
    @Alias("邮箱")
    private String email;

    @ApiModelProperty("地址")
    @Alias("地址")
    private String address;

    @ApiModelProperty("唯一标识")
    @Alias("唯一标识")
    private String uid;

    @ApiModelProperty("头像")
    @Alias("头像")
    private String avatar;

    //逻辑删除字段
    @TableLogic(value = "0",delval= "id")
    private Integer deleted;//唯一联合字段

    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    /*
        插入与跟新时都写此字段
        若使用FieldFill。UPDATE，则只更新时写字段
     */
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;

    @ApiModelProperty("角色")
    @Alias("角色")
    private String role;

    @ApiModelProperty("个性签名")
    @Alias("个性签名")
    private String sign;
}