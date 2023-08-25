package com.partner.boot.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
//限流操作
@Component
@Slf4j
public class MyFilter implements Filter{
    //时间窗口

    //1秒之内允许通过两个请求

    private static volatile long startTime=System.currentTimeMillis();//实时更新当前最新时间
    private static final long windowTime = 1000L;
    private static final int door=100;
    private static final AtomicInteger bear = new AtomicInteger(0);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //只要发生一次请求，就进行
        int count = bear.incrementAndGet();//来一个请求，就加一
        if (count==1) {//并发安全
            startTime = System.currentTimeMillis();
        }
        long now= System.currentTimeMillis();//发生了请求
        log.info("拦截请求，count:{}",count);
        // 0 -> 1 1 -> 2 2->3
        log.info("时间窗口：{}ms,count:{}",(now-startTime),count);
        if(now - startTime <= windowTime){
            if(count > door){//超过了阀值
                //进行限制
                log.info("拦截成功,拦截了请求count：{}",count);
                HttpServletResponse response =(HttpServletResponse) servletResponse;
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_CBOR_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                response.getWriter().print(JSONUtil.toJsonStr(Result.error("402","接口请求太频繁")));
                return;  //关闸
            }
        }else{
            //重新进入下一个窗口
            startTime = System.currentTimeMillis();
            bear.set(1);
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        filterChain.doFilter(servletRequest,servletResponse);//可以正常通过
        log.info("接口请求的路径:{}",request.getServletPath());
    }
}