package com.chatsphere.repository;

import com.chatsphere.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByDmKeyOrderByCreatedAtAsc(String dmKey, Pageable pageable);

    // Find pinned message in a DM conversation
    @Query("SELECT m FROM Message m WHERE m.dmKey = :dmKey AND m.pinned = true")
    Optional<Message> findPinnedByDmKey(@Param("dmKey") String dmKey);

    // Find all DM messages where user is sender or receiver
    @Query("SELECT m FROM Message m WHERE m.sender = :username OR m.receiver = :username ORDER BY m.createdAt DESC")
    List<Message> findBySenderOrReceiver(@Param("username") String username);
}