package com.chatapp.repository;

import com.chatapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select u from User u where u.id<>:currentId and (lower(u.username) like lower(concat('%',:keyword,'%')) or lower(u.email) like lower(concat('%',:keyword,'%')) or lower(u.displayName) like lower(concat('%',:keyword,'%')))")
    Page<User> search(@Param("currentId") Long currentId, @Param("keyword") String keyword, Pageable pageable);
}
