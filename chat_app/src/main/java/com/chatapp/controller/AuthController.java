package com.chatapp.controller;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.service.AuthService;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth; private final UserService users;
    public AuthController(AuthService auth, UserService users) { this.auth=auth; this.users=users; }
    @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request)); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @GetMapping("/me") public ProfileDto me(Authentication authentication) { return users.me(authentication); }
    @PostMapping("/logout") public ResponseEntity<Void> logout() { return ResponseEntity.noContent().build(); }
}
