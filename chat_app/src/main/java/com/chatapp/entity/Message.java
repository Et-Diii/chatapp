package com.chatapp.entity;

import com.chatapp.entity.Enums.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "messages", indexes = {@Index(name = "idx_message_conversation_created", columnList = "conversation_id,created_at"), @Index(name = "idx_message_conversation_read", columnList = "conversation_id,is_read")})
@Getter @Setter @NoArgsConstructor
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conversation_id", nullable = false) private Conversation conversation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sender_id", nullable = false) private User sender;
    @Column(nullable = false, length = 4000) private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private MessageType messageType = MessageType.TEXT;
    @Column(name = "is_read", nullable = false) private boolean isRead;
    @Column(name="created_at", nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
