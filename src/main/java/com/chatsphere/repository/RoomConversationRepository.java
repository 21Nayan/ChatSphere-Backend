package com.chatsphere.repository;

import com.chatsphere.model.RoomConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomConversationRepository extends JpaRepository<RoomConversation, Long> {

    List<RoomConversation> findByRoomIdOrderByCreatedAtAsc(Long roomId, Pageable pageable);

    // Find pinned message in a room
    @Query("SELECT m FROM RoomConversation m WHERE m.roomId = :roomId AND m.pinned = true")
    Optional<RoomConversation> findPinnedByRoomId(@Param("roomId") Long roomId);
}