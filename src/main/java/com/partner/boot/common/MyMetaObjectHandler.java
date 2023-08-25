package com.partner.boot.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill......");
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
        this.strictInsertFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
        //或者
        //this.strictInsertFill(metaObject,"createTime",()->LocalDateTime.now(),LocalDateTime.class);
        //或者
        //this .fillStrategy(metaObject,"createTime",LocalDateTime.now());
    }
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill......");
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
        //或者
        //this.strictUpdateFill(metaObject,"updateTime",()->LocalDateTime.now(),LocalDateTime.class);
        //或者
        //this .fillStrategy(metaObject,"updateTime",LocalDateTime.now());
    }
}
