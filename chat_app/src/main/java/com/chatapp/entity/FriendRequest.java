package com.chatapp.entity;

import com.chatapp.entity.Enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "friend_requests", indexes = {@Index(name = "idx_request_sender_status", columnList = "sender_id,status"), @Index(name = "idx_request_receiver_status", columnList = "receiver_id,status")})
@Getter @Setter @NoArgsConstructor
public class FriendRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sender_id", nullable = false) private User sender;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "receiver_id", nullable = false) private User receiver;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private RequestStatus status = RequestStatus.PENDING;
    @Column(name="pending_pair_key", unique=true, length=64) private String pendingPairKey;
    @Column(name="created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name="updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
