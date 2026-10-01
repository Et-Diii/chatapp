package com.chatapp.config;

import com.chatapp.security.StompAuthChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration @EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompAuthChannelInterceptor auth;
    private final String allowedOrigins;
    public WebSocketConfig(StompAuthChannelInterceptor auth, @Value("${chat.allowed-origins}") String allowedOrigins) { this.auth=auth; this.allowedOrigins=allowedOrigins; }
    @Override public void configureMessageBroker(MessageBrokerRegistry registry) { registry.enableSimpleBroker("/queue"); registry.setApplicationDestinationPrefixes("/app"); registry.setUserDestinationPrefix("/user"); }
    @Override public void registerStompEndpoints(StompEndpointRegistry registry) { registry.addEndpoint("/ws").setAllowedOriginPatterns(java.util.Arrays.stream(allowedOrigins.split(",")).map(String::trim).toArray(String[]::new)); }
    @Override public void configureClientInboundChannel(ChannelRegistration registration) { registration.interceptors(auth); }
}
