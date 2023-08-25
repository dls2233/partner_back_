package com.partner.boot.common;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.partner.boot.entity.User;
import com.partner.boot.mapper.UserMapper;
import com.partner.boot.service.IUserService;
import com.partner.boot.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class InitRunner implements ApplicationRunner {

    @Resource
    UserMapper userMapper;
    //在项目启动成功后运行此方法
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            RedisUtils.ping();//redis数据探测，初始化连接
            //数据库探测:在项目启动时，通过查询一次数据的方法以此来启动数据库，防止数据库的懒加载
            userMapper.select1();
            log.info("启动项目数据库连接查询成功");

            //发送一次一步web请求来初始化连接
            ThreadUtil.execAsync(() -> {
                HttpUtil.get("http://localhost:9090/");
                log.info("启动项目tomcat连接查询成功");
            });
        }catch (Exception e){
            log.info("启动优化失败");
        }
    }
}
