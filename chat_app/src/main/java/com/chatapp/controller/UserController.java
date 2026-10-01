package com.chatapp.controller;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users=users; }
    @GetMapping("/me") public ProfileDto me(Authentication auth) { return users.me(auth); }
    @PutMapping("/me") public ProfileDto update(@Valid @RequestBody ProfileUpdateRequest body, Authentication auth) { return users.update(body,auth); }
    @GetMapping("/search") public List<SearchUserDto> search(@RequestParam(required=false) String keyword, Authentication auth) { return users.search(keyword,auth); }
    @GetMapping("/{id}") public UserDto get(@PathVariable Long id) { return users.get(id); }
}
