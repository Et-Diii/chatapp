package com.chatapp.repository;

import com.chatapp.entity.FriendRequest;
import com.chatapp.entity.Enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, RequestStatus status);
    boolean existsBySenderIdAndReceiverIdAndStatusAndIdNot(Long senderId, Long receiverId, RequestStatus status, Long id);
    List<FriendRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(Long receiverId, RequestStatus status);
    List<FriendRequest> findBySenderIdAndStatusOrderByCreatedAtDesc(Long senderId, RequestStatus status);
    Optional<FriendRequest> findByIdAndReceiverId(Long id, Long receiverId);
    Optional<FriendRequest> findByIdAndSenderId(Long id, Long senderId);
}
