package com.chatapp.service;

import com.chatapp.dto.ApiDtos.MessageDto;
import com.chatapp.dto.ApiDtos.ReadReceiptDto;
import com.chatapp.entity.Message;
import com.chatapp.entity.Enums.MessageType;
import com.chatapp.exception.ForbiddenException;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.repository.ConversationMemberRepository;
import com.chatapp.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class MessageService {
    private final MessageRepository messages; private final ConversationService conversations; private final ConversationMemberRepository members;
    private final CurrentUserService current; private final DtoMapper mapper; private final SimpMessagingTemplate messaging;
    public MessageService(MessageRepository messages, ConversationService conversations, ConversationMemberRepository members, CurrentUserService current, DtoMapper mapper, SimpMessagingTemplate messaging) {
        this.messages=messages; this.conversations=conversations; this.members=members; this.current=current; this.mapper=mapper; this.messaging=messaging;
    }
    @Transactional public MessageDto send(Long conversationId, String content, org.springframework.security.core.Authentication auth) {
        var sender=current.get(auth); conversations.requireMember(conversationId,sender.getId());
        String clean=content == null ? "" : content.trim();
        if (clean.isEmpty()) throw new com.chatapp.exception.BadRequestException("Message cannot be empty.");
        Message message=new Message(); message.setConversation(conversations.conversation(conversationId)); message.setSender(sender); message.setContent(clean); message.setMessageType(MessageType.TEXT);
        message=messages.save(message);
        var conversation=message.getConversation(); conversation.setUpdatedAt(Instant.now());
        MessageDto dto=mapper.message(message);
        members.findByConversationId(conversationId).forEach(member -> messaging.convertAndSendToUser(member.getUser().getUsername(),"/queue/messages",dto));
        return dto;
    }
    @Transactional public void markRead(Long messageId, org.springframework.security.core.Authentication auth) {
        var user=current.get(auth);
        Message message=messages.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        conversations.requireMember(message.getConversation().getId(),user.getId());
        if (!message.getSender().getId().equals(user.getId()) && !message.isRead()) {
            message.setRead(true);
            messaging.convertAndSendToUser(message.getSender().getUsername(),"/queue/messages",new ReadReceiptDto("READ",message.getId(),message.getConversation().getId(),user.getId(),true));
        }
    }
    @Transactional public int markConversationRead(Long conversationId, org.springframework.security.core.Authentication auth) {
        var user=current.get(auth); Long userId=user.getId(); conversations.requireMember(conversationId,userId);
        var unread=messages.findByConversationIdAndSenderIdNotAndIsReadFalse(conversationId,userId);
        unread.forEach(message -> {
            message.setRead(true);
            messaging.convertAndSendToUser(message.getSender().getUsername(),"/queue/messages",new ReadReceiptDto("READ",message.getId(),conversationId,userId,true));
        });
        return unread.size();
    }
}
