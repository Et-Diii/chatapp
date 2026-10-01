package com.chatapp.service;

import com.chatapp.entity.User;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository users;
    public CurrentUserService(UserRepository users) { this.users = users; }
    public User get(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) throw new ResourceNotFoundException("Authenticated user not found.");
        return users.findByUsernameIgnoreCaseOrEmailIgnoreCase(authentication.getName(), authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }
    public User byId(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found.")); }
}
