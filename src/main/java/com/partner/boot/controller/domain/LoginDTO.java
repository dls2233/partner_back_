package com.partner.boot.controller.domain;

import com.partner.boot.entity.Permission;
import com.partner.boot.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO implements Serializable {//实现序列化接口
    private static final long serialVersionUID = 1L;
    private User user;
    private String token;
    private List<Permission> menus;
    private List<Permission> auths;
}
