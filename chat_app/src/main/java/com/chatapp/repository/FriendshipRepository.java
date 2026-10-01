package com.chatapp.repository;

import com.chatapp.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("select f from Friendship f where (f.user1.id=:a and f.user2.id=:b) or (f.user1.id=:b and f.user2.id=:a)")
    List<Friendship> findPair(@Param("a") Long a, @Param("b") Long b);
    @Query("select f from Friendship f join fetch f.user1 join fetch f.user2 where f.user1.id=:id or f.user2.id=:id")
    List<Friendship> findAllForUser(@Param("id") Long id);
    @Modifying @Query("delete from Friendship f where (f.user1.id=:a and f.user2.id=:b) or (f.user1.id=:b and f.user2.id=:a)")
    int deletePair(@Param("a") Long a, @Param("b") Long b);
}
