package com.chatapp.security;

import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwt; private final CustomUserDetailsService users;
    public StompAuthChannelInterceptor(JwtService jwt, CustomUserDetailsService users) { this.jwt=jwt; this.users=users; }
    @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor=MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if (accessor==null) return message;
        StompCommand command=accessor.getCommand();
        if (command==StompCommand.CONNECT) {
            String header=accessor.getFirstNativeHeader("Authorization");
            if (header==null || !header.startsWith("Bearer ")) throw new IllegalArgumentException("Authentication required.");
            String token=header.substring(7); String subject=jwt.subject(token); UserDetails user=users.loadUserByUsername(subject);
            if (!jwt.isValid(token,user)) throw new IllegalArgumentException("Invalid or expired token.");
            accessor.setUser(new UsernamePasswordAuthenticationToken(user.getUsername(),null,user.getAuthorities()));
        } else if (command==StompCommand.SEND) {
            if (accessor.getUser()==null || !"/app/chat".equals(accessor.getDestination())) throw new IllegalArgumentException("Not authorized.");
        } else if (command==StompCommand.SUBSCRIBE) {
            if (accessor.getUser()==null || !"/user/queue/messages".equals(accessor.getDestination())) throw new IllegalArgumentException("Not authorized.");
        } else if (command==StompCommand.DISCONNECT) {
            // The connection was already authenticated during CONNECT.
        }
        return message;
    }
}
