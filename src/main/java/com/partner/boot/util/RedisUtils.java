package com.partner.boot.util;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
@SuppressWarnings(value = {"unchecked"})
@Component
@Slf4j
public class RedisUtils<T> {
    private static RedisTemplate<String,Object> staticRedisTemplate;
    private final RedisTemplate<String,Object> redisTemplate;

    public RedisUtils(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    //Springboot启动成功后调用此方法
    @PostConstruct
    public void initRedis(){
        //初始化设置 静态staticRedisTemplate对象，方便后续操作数据
        staticRedisTemplate = redisTemplate;
    }
    //设置缓存基本对象，integer，String，实体类等
    public static <T> void setCacheObject(final String key,final T value){
        staticRedisTemplate.opsForValue().set(key,value);
    }
    //设置有过期时间
    public static <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit){
        staticRedisTemplate.opsForValue().set(key,value,timeout,timeUnit);
    }
    //获得缓存基本对象
    public static <T>  T getCacheObject(final String key){
        return (T)staticRedisTemplate.opsForValue().get(key);
    }
    //删除多个对象
    public static boolean deleteObject(final String key){
        return Boolean.TRUE.equals(staticRedisTemplate.delete(key));
    }
    //获取时间
    public static Long getExpireTime(final String key){
        return staticRedisTemplate.getExpire(key);
    }
    //发送ping命令
    //redis返回pong
    public static void ping(){
        String res=staticRedisTemplate.execute(RedisConnectionCommands::ping);
        log.info("Redis ping === {}",res);
    }

}
