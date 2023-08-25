package com.partner.boot.common;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfigure {
    //sa-token整合jwt
    @Bean
    public StpLogic getStpLogicJwt(){
        return new StpLogicJwtForSimple();
    }
}
