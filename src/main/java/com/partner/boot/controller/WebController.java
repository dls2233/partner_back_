package com.partner.boot.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.partner.boot.common.Result;
import com.partner.boot.controller.domain.LoginDTO;
import com.partner.boot.controller.domain.UserRequest;
import com.partner.boot.entity.User;
import com.partner.boot.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.StandardProtocolFamily;

@Api(tags = "无权限接口列表")
@RestController
@Slf4j
public class WebController {

    @Resource
    IUserService userService;

    @GetMapping(value = "/")
    public String version() {
        String ver = "partner-back-0.0.1-SNAPSHOT";  // 应用版本号
        Package aPackage = WebController.class.getPackage();
        String title = aPackage.getImplementationTitle();
        String version = aPackage.getImplementationVersion();
        if (title != null && version != null) {
            ver = String.join("-", title, version);
        }
        return ver;
    }

    @ApiOperation(value="用户登录接口")//注释
    @PostMapping("/login")
    public Result login(@RequestBody UserRequest user){//解析出请求的参数(包含登录名和密码)
        long startTime = System.currentTimeMillis();
        LoginDTO res = userService.login(user);
        log.info("登陆时间 {}ms",System.currentTimeMillis()-startTime);
        return Result.success(res);
    }

    @ApiOperation(value="用户退出登录接口")//注释
    @GetMapping("/logout/{uid}")
    public Result logout(@PathVariable String uid ){//解析出请求的参数(包含登录名和密码)
        userService.logout(uid);
        return Result.success();
    }

    @ApiOperation(value="用户注册接口")//注释
    @PostMapping("/register")
    public Result register(@RequestBody UserRequest user){//解析出请求的参数(包含登录名和密码)
        userService.register(user);
        return Result.success();
    }
    @ApiOperation(value = "邮箱接口验证")
    @GetMapping("/email")
    public Result sendEmail(@RequestParam String email,@RequestParam String type) { //?email=xxx&type=xxx
        Long start=System.currentTimeMillis();
        userService.sendEmail(email,type);
        log.info("发送邮件花费的时间：{}ms",System.currentTimeMillis()-start);
        return Result.success();
    }
    @ApiOperation(value = "密码重置接口")
    @PostMapping("/password/reset")
    public Result passwordReset(@RequestBody UserRequest userRequest) { //?email=xxx&type=xxx
        String newPass=userService.passwordReset(userRequest);
        return Result.success(newPass);
    }
    //修改密码
    @PostMapping("/password/change")
    public Result passwordChange(@RequestBody UserRequest userRequest){
        userService.passwordChange(userRequest);
        return Result.success();
    }
    //更新个人信息
    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody User user){
        Object loginId = StpUtil.getLoginId();
        if(!loginId.equals(user.getUid())){
            Result.error("无权限");
        }
        userService.updateById(user);
        return Result.success(user);
    }

}
