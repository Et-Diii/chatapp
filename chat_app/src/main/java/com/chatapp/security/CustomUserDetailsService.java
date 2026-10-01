package com.chatapp.security;

import com.chatapp.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String identifier) {
        var user = users.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(user.getUsername()).password(user.getPassword()).authorities("USER").build();
    }
}
