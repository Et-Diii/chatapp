package com.chatapp.websocket;

import com.chatapp.entity.Enums.UserStatus;
import com.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PresenceListener implements CommandLineRunner {
    private final UserRepository users;
    private final ConcurrentHashMap<String,AtomicInteger> sessions=new ConcurrentHashMap<>();
    public PresenceListener(UserRepository users) { this.users=users; }
    @Override @Transactional public void run(String... args) { users.findAll().forEach(user -> user.setStatus(UserStatus.OFFLINE)); }
    @EventListener @Transactional public void connected(SessionConnectEvent event) {
        Principal user=event.getUser(); if (user==null) return;
        if (sessions.computeIfAbsent(user.getName(),k->new AtomicInteger()).incrementAndGet()==1) setStatus(user.getName(),UserStatus.ONLINE);
    }
    @EventListener @Transactional public void disconnected(SessionDisconnectEvent event) {
        Principal user=event.getUser(); if (user==null) return;
        sessions.computeIfPresent(user.getName(),(key,count)->count.decrementAndGet()<=0 ? null : count);
        if (!sessions.containsKey(user.getName())) setStatus(user.getName(),UserStatus.OFFLINE);
    }
    private void setStatus(String username, UserStatus status) { users.findByUsernameIgnoreCaseOrEmailIgnoreCase(username,username).ifPresent(user -> user.setStatus(status)); }
}
