package com.chatapp.controller;

import com.chatapp.dto.ApiDtos.UserDto;
import com.chatapp.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friends;
    public FriendController(FriendService friends) { this.friends=friends; }
    @GetMapping public List<UserDto> list(Authentication auth) { return friends.list(auth); }
    @DeleteMapping("/{userId}") public ResponseEntity<Void> remove(@PathVariable Long userId, Authentication auth) { friends.remove(userId,auth); return ResponseEntity.noContent().build(); }
}
