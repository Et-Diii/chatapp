package com.chatapp.service;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.entity.Conversation;
import com.chatapp.entity.ConversationMember;
import com.chatapp.entity.Enums.ConversationType;
import com.chatapp.exception.BadRequestException;
import com.chatapp.exception.ForbiddenException;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.repository.ConversationMemberRepository;
import com.chatapp.repository.ConversationRepository;
import com.chatapp.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ConversationService {
    private final ConversationRepository conversations; private final ConversationMemberRepository members; private final MessageRepository messages;
    private final CurrentUserService current; private final FriendService friends; private final DtoMapper mapper;
    public ConversationService(ConversationRepository conversations, ConversationMemberRepository members, MessageRepository messages, CurrentUserService current, FriendService friends, DtoMapper mapper) {
        this.conversations=conversations; this.members=members; this.messages=messages; this.current=current; this.friends=friends; this.mapper=mapper;
    }
    @Transactional public ConversationDto createPrivate(Long otherId, org.springframework.security.core.Authentication auth) {
        var me=current.get(auth); var other=current.byId(otherId);
        if (me.getId().equals(otherId)) throw new BadRequestException("You cannot start a conversation with yourself.");
        if (!friends.areFriends(me.getId(),otherId)) throw new ForbiddenException("You can only start conversations with friends.");
        String key=key(me.getId(),otherId);
        Conversation conversation=conversations.findByPrivateKey(key).orElseGet(() -> {
            Conversation created=new Conversation(); created.setType(ConversationType.PRIVATE); created.setPrivateKey(key); created=conversations.save(created);
            ConversationMember first=new ConversationMember(); first.setConversation(created); first.setUser(me); members.save(first);
            ConversationMember second=new ConversationMember(); second.setConversation(created); second.setUser(other); members.save(second);
            return created;
        });
        return summary(conversation, me.getId());
    }
    @Transactional(readOnly=true) public List<ConversationDto> list(org.springframework.security.core.Authentication auth) {
        Long userId=current.get(auth).getId();
        return members.findAllForUser(userId).stream().map(member -> summary(member.getConversation(),userId)).toList();
    }
    @Transactional(readOnly=true) public ConversationDetailDto detail(Long id, org.springframework.security.core.Authentication auth) {
        var me=current.get(auth); Conversation conversation=conversation(id); requireMember(id,me.getId());
        List<UserDto> participants=members.findByConversationId(id).stream().map(m -> mapper.user(m.getUser())).toList();
        return new ConversationDetailDto(conversation.getId(),conversation.getType(),participants,conversation.getCreatedAt(),conversation.getUpdatedAt());
    }
    @Transactional(readOnly=true) public org.springframework.data.domain.Page<MessageDto> history(Long id, int page, int size, org.springframework.security.core.Authentication auth) {
        requireMember(id,current.get(auth).getId());
        int safePage=Math.max(0,page), safeSize=Math.min(100,Math.max(1,size));
        return messages.findByConversationIdOrderByCreatedAtAsc(id,PageRequest.of(safePage,safeSize)).map(mapper::message);
    }
    public Conversation conversation(Long id) { return conversations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conversation not found.")); }
    public void requireMember(Long conversationId, Long userId) {
        if (!members.existsByConversationIdAndUserId(conversationId,userId)) throw new ForbiddenException("You do not have access to this conversation.");
    }
    private ConversationDto summary(Conversation conversation, Long userId) {
        var other=members.findByConversationId(conversation.getId()).stream().map(ConversationMember::getUser).filter(u -> !u.getId().equals(userId)).findFirst().orElseThrow();
        var last=messages.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId(),Sort.by(Sort.Direction.DESC,"createdAt")).stream().findFirst().map(mapper::message).orElse(null);
        return new ConversationDto(conversation.getId(),conversation.getType(),mapper.user(other),last,messages.countUnread(conversation.getId(),userId),conversation.getUpdatedAt());
    }
    private String key(Long a, Long b) { return Math.min(a,b)+":"+Math.max(a,b); }
}
