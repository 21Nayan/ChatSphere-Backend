package com.chatsphere.repository;

import com.chatsphere.model.PinnedChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PinnedChatRepository extends JpaRepository<PinnedChat, Long> {
    List<PinnedChat>     findByUsernameOrderByPinnedAtDesc(String username);
    Optional<PinnedChat> findByUsernameAndChatKey(String username, String chatKey);
    boolean              existsByUsernameAndChatKey(String username, String chatKey);
    void                 deleteByUsernameAndChatKey(String username, String chatKey);
}