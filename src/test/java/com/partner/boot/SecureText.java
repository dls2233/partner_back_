package com.partner.boot;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.junit.jupiter.api.Test;

public class SecureText {
    //对称加密
    @Test
    public void text(){
        //定义密钥和明文
        String key="123456";
        String text="Sa-Token,一个轻量级java权限认证框架";

        //加密
        String ciphertext= SaSecureUtil.aesEncrypt(key,text);
        System.out.println("AES加密后："+ciphertext);

        //解密
        String text2 = SaSecureUtil.aesDecrypt(key,ciphertext);
        System.out.println("AES解密后："+text2);
    }
}
