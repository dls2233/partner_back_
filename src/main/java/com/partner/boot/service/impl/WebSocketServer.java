package com.partner.boot.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.partner.boot.entity.Im;
import com.partner.boot.entity.User;
import com.partner.boot.service.IImService;
import com.partner.boot.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author websocket服务
 */
@ServerEndpoint(value = "/imserver/{uid}")
@Component
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * 记录当前在线连接数
     */
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Resource
    IUserService userService;
    @Resource
    IImService imService;


    private static IUserService staticUserService;
    private static IImService staticImService;

    // 程序初始化的时候触发这个方法  赋值
    @PostConstruct
    public void setStaticUser() {
        staticUserService = userService;
        staticImService = imService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        sessionMap.put(uid, session);
        log.info("有新用户加入，uid={}, 当前在线人数为：{}", uid, sessionMap.size());
        Dict dict = Dict.create().set("nums", sessionMap.size());
        sendAllMessage(JSONUtil.toJsonStr(dict));  // 后台发送消息给所有的客户端
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("uid") String uid) {
        sessionMap.remove(uid);
        log.info("有一连接关闭，uid={}的用户session, 当前在线人数为：{}", uid, sessionMap.size());
        Dict dict = Dict.create().set("nums", sessionMap.size());
        sendAllMessage(JSONUtil.toJsonStr(dict));  // 后台发送消息给所有的客户端
    }

    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session fromSession, @PathParam("uid") String uid) throws JsonProcessingException {
        log.info("服务端收到用户uid={}的消息:{}", uid, message);
        // 处理msg
        // 存储数据库
        // 添加创建时间
        if (staticUserService == null) {
            return;
        }
        User user = staticUserService.getOne(new QueryWrapper<User>().eq("uid", uid));
        if (user == null) {
            log.error("获取用户信息失败，uid={}", uid);
            return;
        }

        Im im = Im.builder().uid(uid).username(user.getName()).avatar(user.getAvatar()).sign(user.getSign())
                .createTime(LocalDateTime.now()).text(message).build();
        // 存储数据到数据库
        staticImService.save(im);
        String jsonStr = new ObjectMapper().writeValueAsString(im);  // 处理后的消息体
        this.sendAllMessage(jsonStr);
        log.info("发送消息：{}", jsonStr);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给除了自己的其他客户端
     */
    private void sendMessage(Session fromSession, String message) {
        sessionMap.values().forEach(session -> {
            if (fromSession != session) {
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("服务端发送消息给客户端异常", e);
                }
            }
        });
    }

    /**
     * 服务端发送消息给所有客户端
     */
    private void sendAllMessage(String message) {
        try {
            for (Session session : sessionMap.values()) {
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }
}

