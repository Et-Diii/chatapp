package com.chatapp.service;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.entity.User;
import com.chatapp.entity.Enums.UserStatus;
import com.chatapp.exception.BadRequestException;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager;
    private final JwtService jwt; private final DtoMapper mapper;
    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwt, DtoMapper mapper) {
        this.users=users; this.encoder=encoder; this.authenticationManager=authenticationManager; this.jwt=jwt; this.mapper=mapper;
    }
    @Transactional public AuthResponse register(RegisterRequest request) {
        String username=request.username().trim(); String email=request.email().trim().toLowerCase();
        if (users.existsByUsernameIgnoreCase(username)) throw new BadRequestException("That username is already in use.");
        if (users.existsByEmailIgnoreCase(email)) throw new BadRequestException("That email is already registered.");
        User user=new User(); user.setUsername(username); user.setEmail(email); user.setPassword(encoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim()); user.setStatus(UserStatus.OFFLINE); users.save(user);
        UserDetails details=org.springframework.security.core.userdetails.User.withUsername(user.getUsername()).password(user.getPassword()).authorities("USER").build();
        return new AuthResponse(jwt.generate(details), "Bearer", mapper.user(user));
    }
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.identifier().trim(), request.password()));
        User user=users.findByUsernameIgnoreCaseOrEmailIgnoreCase(request.identifier().trim(), request.identifier().trim()).orElseThrow();
        UserDetails details=org.springframework.security.core.userdetails.User.withUsername(user.getUsername()).password(user.getPassword()).authorities("USER").build();
        return new AuthResponse(jwt.generate(details), "Bearer", mapper.user(user));
    }
}
