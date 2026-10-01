package com.chatapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "friendships", uniqueConstraints = @UniqueConstraint(name = "uk_friend_pair", columnNames = {"user1_id", "user2_id"}), indexes = {@Index(name = "idx_friend_user1", columnList = "user1_id"), @Index(name = "idx_friend_user2", columnList = "user2_id")})
@Getter @Setter @NoArgsConstructor
public class Friendship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user1_id", nullable = false) private User user1;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user2_id", nullable = false) private User user2;
    @Column(name="created_at", nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
