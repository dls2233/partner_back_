package com.partner.boot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.partner.boot.common.LDTConfig;
import lombok.Getter;
import lombok.Setter;

/**
* <p>
* 动态
* </p>
*
* @author dalaoshi
* @since 2023-08-19
*/
@Getter
@Setter
@TableName("dynamic")
@ApiModel(value = "Dynamic对象", description = "动态")
public class Dynamic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 标题
    @ApiModelProperty("标题")
    @Alias("标题")
    private String name;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 图片
    @ApiModelProperty("图片")
    @Alias("图片")
    private String img;

    // 简介
    @ApiModelProperty("简介")
    @Alias("简介")
    private String description;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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

    // 用户标识
    @ApiModelProperty("用户标识")
    @Alias("用户标识")
    private String uid;

    // 删除标识
    @ApiModelProperty("删除标识")
    @Alias("删除标识")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    @ApiModelProperty("浏览次数")
    @Alias("浏览次数")
    private Integer view;

    //数据库不存在的字段
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Boolean hasPraise;

    @TableField(exist = false)
    private Boolean hasCollect;

    @TableField(exist = false)
    private Integer hot;

    //点赞
    @TableField(exist = false)
    private Integer praiseCount;

    //收藏
    @TableField(exist = false)
    private Integer collectCount;

    //评论
    @TableField(exist = false)
    private Integer commentCount;
}