package com.chatapp.controller;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.service.ConversationService;
import com.chatapp.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversations; private final MessageService messages;
    public ConversationController(ConversationService conversations, MessageService messages) { this.conversations=conversations; this.messages=messages; }
    @PostMapping("/private/{userId}") public ResponseEntity<ConversationDto> create(@PathVariable Long userId, Authentication auth) { return ResponseEntity.status(HttpStatus.CREATED).body(conversations.createPrivate(userId,auth)); }
    @GetMapping public List<ConversationDto> list(Authentication auth) { return conversations.list(auth); }
    @GetMapping("/{conversationId}") public ConversationDetailDto detail(@PathVariable Long conversationId, Authentication auth) { return conversations.detail(conversationId,auth); }
    @GetMapping("/{conversationId}/messages") public PageResponse<MessageDto> messages(@PathVariable Long conversationId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="30") int size, Authentication auth) {
        Page<MessageDto> result=conversations.history(conversationId,page,size,auth);
        return new PageResponse<>(result.getContent(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages(),result.isLast());
    }
    @PostMapping("/{conversationId}/messages") public ResponseEntity<MessageDto> send(@PathVariable Long conversationId, @Valid @RequestBody MessageRequest request, Authentication auth) { return ResponseEntity.status(HttpStatus.CREATED).body(messages.send(conversationId,request.content(),auth)); }
    @PutMapping("/{conversationId}/read") public ResponseEntity<Void> markRead(@PathVariable Long conversationId, Authentication auth) { messages.markConversationRead(conversationId,auth); return ResponseEntity.noContent().build(); }
}
