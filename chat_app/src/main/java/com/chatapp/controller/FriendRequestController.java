package com.chatapp.controller;

import com.chatapp.dto.ApiDtos.FriendRequestDto;
import com.chatapp.service.FriendRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/friend-requests")
public class FriendRequestController {
    private final FriendRequestService requests;
    public FriendRequestController(FriendRequestService requests) { this.requests=requests; }
    @PostMapping("/{userId}") public ResponseEntity<FriendRequestDto> send(@PathVariable Long userId, Authentication auth) { return ResponseEntity.status(HttpStatus.CREATED).body(requests.send(userId,auth)); }
    @GetMapping("/received") public List<FriendRequestDto> received(Authentication auth) { return requests.received(auth); }
    @GetMapping("/sent") public List<FriendRequestDto> sent(Authentication auth) { return requests.sent(auth); }
    @PutMapping("/{id}/accept") public FriendRequestDto accept(@PathVariable Long id, Authentication auth) { return requests.accept(id,auth); }
    @PutMapping("/{id}/reject") public FriendRequestDto reject(@PathVariable Long id, Authentication auth) { return requests.reject(id,auth); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication auth) { requests.cancel(id,auth); return ResponseEntity.noContent().build(); }
}
