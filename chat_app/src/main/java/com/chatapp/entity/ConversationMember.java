package com.chatapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "conversation_members", uniqueConstraints = @UniqueConstraint(name = "uk_conversation_member", columnNames = {"conversation_id", "user_id"}), indexes = {@Index(name = "idx_member_conversation", columnList = "conversation_id"), @Index(name = "idx_member_user", columnList = "user_id")})
@Getter @Setter @NoArgsConstructor
public class ConversationMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conversation_id", nullable = false) private Conversation conversation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name="joined_at", nullable = false, updatable = false) private Instant joinedAt;
    @PrePersist void onCreate() { joinedAt = Instant.now(); }
}
