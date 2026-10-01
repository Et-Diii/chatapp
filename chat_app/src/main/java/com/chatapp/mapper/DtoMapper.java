package com.chatapp.mapper;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public UserDto user(User user) { return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl(), user.getStatus(), user.getCreatedAt()); }
    public ProfileDto profile(User user) { return new ProfileDto(user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.getStatus(), user.getCreatedAt()); }
    public FriendRequestDto request(FriendRequest request) { return new FriendRequestDto(request.getId(), user(request.getSender()), user(request.getReceiver()), request.getStatus(), request.getCreatedAt()); }
    public MessageDto message(Message message) { return new MessageDto(message.getId(), message.getConversation().getId(), message.getSender().getId(), message.getSender().getUsername(), message.getContent(), message.getMessageType(), message.isRead(), message.getCreatedAt()); }
}
