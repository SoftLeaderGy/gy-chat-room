package com.yang.gychatroom.ws;

import com.alibaba.fastjson.JSONObject;
import com.yang.gychatroom.config.GetHttpSessionConfigurator;
import com.yang.gychatroom.pojo.Message;
import com.yang.gychatroom.pojo.ResultMessage;
import com.yang.gychatroom.util.MessageUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Author: Guo.Yang
 * @Date: 2022/04/10/17:51
 */
@ServerEndpoint(value = "/chat",configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndPoint {


    // 用来存储每一个客户端对象对应的ChatEndPoint对象
    private static Map<String,ChatEndPoint> onLineUsers = new ConcurrentHashMap<>();

    // 声明Session对象,通过该对象可以发送消息给不同的客户端
    public Session session;

    // 声明一个HttpSession对象,我们之前在HttpSession对象中存储了用户名
    public HttpSession httpSession;
    @OnOpen
    // 连接建立时被调用
    public void onOpen(Session session, EndpointConfig endpointConfig){
        // 将局部的session对象赋值给成员对象
        this.session = session;

        // 获取HttpSession对象
        HttpSession httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        this.httpSession = httpSession;

        // 获取用户登录的用户名称
        String username = (String) httpSession.getAttribute("username");

        // 将用户名作为key 当前的ChatEndPoint对象 也就是this作为value存入map中
        onLineUsers.put(username,this);

        // 将当前的在线的所有用户名推送给所有的客户端
        // 1.获取数据
        String message = MessageUtils.getMessage(true, null, getUsers());
        // 2.调用方法进行系统推送
        broadcastAllUsers(message);
    }

    @SneakyThrows
    private void broadcastAllUsers(String message) {
        // 要将消息发送给所有的客户端
        // 遍历所有的在线的用户名
        for (String s : onLineUsers.keySet()) {
            // 通过在线的map的在线用户名称获取对应的ChatEndPoint对象
            ChatEndPoint chatEndPoint = onLineUsers.get(s);
            // 通过ChatEndPoint对象中的session的BasicRemote对象的sendText方法发送消息
            if(session.isOpen()){
                chatEndPoint.session.getBasicRemote().sendText(message);
            }
        }

    }

    private Set<String> getUsers() {
        return ChatEndPoint.onLineUsers.keySet();
    }

    @OnMessage
    @SneakyThrows
    // 接收客户端发送的数据时被调用
    public void onMessage(String massage,Session session){
        // 接收的是String 首先转成ResultMessage对象
        Message msg = JSONObject.parseObject(massage, Message.class);
        // 获取要发送消息的用户名
        String toName = msg.getToName();
        // 获取发送的消息
        String message = msg.getMessage();
        // 获取当前用户名
        String username = (String) httpSession.getAttribute("username");
        // 获取推送给指定用户的消息格式
        String message1 = MessageUtils.getMessage(false, username, message);
        // 获取被推送的 basicRemote 对象
        RemoteEndpoint.Basic basicRemote = onLineUsers.get(toName).session.getBasicRemote();
        // 通过basicRemote对象进行发送消息
        basicRemote.sendText(message1);
    }

    @OnClose
    // 关闭连接时调用
    public void onClose(Session session){
        String username = (String) httpSession.getAttribute("username");
        //
        ChatEndPoint remove = onLineUsers.remove(username);
        //广播
        String message = MessageUtils.getMessage(true, null, getUsers());
        broadcastAllUsers(message);
    }
}
