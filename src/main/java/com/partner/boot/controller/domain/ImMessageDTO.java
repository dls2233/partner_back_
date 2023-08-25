package com.partner.boot.controller.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Parameter;
import java.util.Date;

@Data
@Builder
public class ImMessageDTO {
    private String uid;
    private String username;
    private String sign;
    private String avatar;
    private String text;
    private Date createTime;
}
