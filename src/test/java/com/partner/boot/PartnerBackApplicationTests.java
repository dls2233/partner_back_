package com.partner.boot;

import com.partner.boot.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PartnerBackApplicationTests {

    @Autowired
    IUserService userService;

    @Test
    void contextLoads() {
        userService.removeById(6);
        System.out.println(userService.getById(6));
    }

}
