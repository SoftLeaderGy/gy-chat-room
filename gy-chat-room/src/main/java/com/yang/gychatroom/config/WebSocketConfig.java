package com.yang.gychatroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Description:
 * @Author: Guo.Yang
 * @Date: 2022/04/10/18:01
 */
@Configuration
public class WebSocketConfig {

    @Bean
    // 注入ServerEndpointExporter bean对象 自动注册使用了@ServerEndpoint 注解
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
