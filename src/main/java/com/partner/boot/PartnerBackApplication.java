package com.partner.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.partner.boot.mapper")
public class PartnerBackApplication {

    public static void main(String[] args) {

        SpringApplication.run(PartnerBackApplication.class, args);
    }

}
