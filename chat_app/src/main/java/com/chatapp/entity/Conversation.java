package com.chatapp.entity;

import com.chatapp.entity.Enums.ConversationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "conversations", indexes = @Index(name = "idx_conversation_updated", columnList = "updated_at"))
@Getter @Setter @NoArgsConstructor
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private ConversationType type = ConversationType.PRIVATE;
    @Column(unique = true, length = 64) private String privateKey;
    @Column(name="created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name="updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
