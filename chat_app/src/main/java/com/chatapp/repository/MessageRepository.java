package com.chatapp.repository;

import com.chatapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);
    Optional<Message> findByIdAndConversationId(Long id, Long conversationId);
    java.util.List<Message> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId, Sort sort);
    java.util.List<Message> findByConversationIdAndSenderIdNotAndIsReadFalse(Long conversationId, Long senderId);
    @Query("select count(m) from Message m where m.conversation.id=:conversationId and m.sender.id<>:userId and m.isRead=false")
    long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    @Modifying @Query("update Message m set m.isRead=true where m.conversation.id=:conversationId and m.sender.id<>:userId and m.isRead=false")
    int markUnreadAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
