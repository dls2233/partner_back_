package com.partner.boot;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Slf4j
public class HttpText {
    @Test
    public void text() throws InterruptedException {
        log.info("开始执行");
        String json = "{\"username\": \"dalaoshi\",\"password\": \"123\"}";
        int count=5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        List<String> list = new ArrayList<>();
        //模拟并发请求
        for(int i=0;i<count;i++){
            int finalI=i;
            new Thread(() -> {
                String res = HttpUtil.post("http://localhost:9090/login",json);
                list.add("第"+finalI+"次请求,返回结果"+res);
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                }catch(InterruptedException e){
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        list.forEach(System.out::println);
    }
}
