package com.chatapp.repository;

import com.chatapp.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
    List<ConversationMember> findByConversationId(Long conversationId);
    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);
    @Query("select m from ConversationMember m join fetch m.conversation c where m.user.id=:userId order by c.updatedAt desc")
    List<ConversationMember> findAllForUser(@Param("userId") Long userId);
}
