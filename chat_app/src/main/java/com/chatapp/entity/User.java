package com.chatapp.entity;

import com.chatapp.entity.Enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users_username", columnList = "username"), @Index(name = "idx_users_email", columnList = "email")})
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 32) private String username;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(nullable = false) private String password;
    @Column(nullable = false, length = 80) private String displayName;
    @Column(length = 2048) private String avatarUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private UserStatus status = UserStatus.OFFLINE;
    @Column(name="created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name="updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
