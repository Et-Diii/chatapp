package com.chatapp.websocket;

import com.chatapp.dto.ApiDtos.ChatRequest;
import com.chatapp.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class ChatWebSocketController {
    private final MessageService messages;
    public ChatWebSocketController(MessageService messages) { this.messages=messages; }
    @MessageMapping("/chat")
    public void chat(@Valid ChatRequest request, Principal principal) {
        if (principal==null) throw new IllegalArgumentException("Authentication required.");
        var auth=new UsernamePasswordAuthenticationToken(principal.getName(),null,java.util.List.of());
        messages.send(request.conversationId(),request.content(),auth);
    }
}
