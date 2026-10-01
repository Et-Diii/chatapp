package com.chatapp.controller;

import com.chatapp.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messages;
    public MessageController(MessageService messages) { this.messages=messages; }
    @PutMapping("/{messageId}/read") public ResponseEntity<Void> read(@PathVariable Long messageId, Authentication auth) { messages.markRead(messageId,auth); return ResponseEntity.noContent().build(); }
}
