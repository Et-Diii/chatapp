package com.chatapp.dto;

import com.chatapp.entity.Enums.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}
    public record RegisterRequest(@NotBlank @Size(min=3,max=32) @Pattern(regexp="[A-Za-z0-9_]+", message="Username may contain letters, numbers, and underscores") String username,
                                  @NotBlank @Email @Size(max=190) String email,
                                  @NotBlank @Size(max=80) String displayName,
                                  @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}
    public record ProfileUpdateRequest(@NotBlank @Size(max=80) String displayName, @Size(max=2048) String avatarUrl) {}
    public record ChatRequest(@NotNull Long conversationId, @NotBlank @Size(max=4000) String content) {}
    public record MessageRequest(@NotBlank @Size(max=4000) String content) {}
    public record UserDto(Long id, String username, String displayName, String avatarUrl, UserStatus status, Instant createdAt) {}
    public record ProfileDto(Long id, String username, String email, String displayName, String avatarUrl, UserStatus status, Instant createdAt) {}
    public record SearchUserDto(Long id, String username, String displayName, String avatarUrl, UserStatus status, String friendshipStatus, Long friendRequestId) {}
    public record AuthResponse(String token, String tokenType, UserDto user) {}
    public record FriendRequestDto(Long id, UserDto sender, UserDto receiver, RequestStatus status, Instant createdAt) {}
    public record MessageDto(Long id, Long conversationId, Long senderId, String senderUsername, String content, MessageType messageType, boolean isRead, Instant createdAt) {}
    public record ReadReceiptDto(String eventType, Long id, Long conversationId, Long senderId, boolean isRead) {}
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {}
    public record ConversationDto(Long id, ConversationType type, UserDto otherUser, MessageDto lastMessage, long unreadCount, Instant updatedAt) {}
    public record ConversationDetailDto(Long id, ConversationType type, List<UserDto> members, Instant createdAt, Instant updatedAt) {}
}
