package com.chatsphere.controller;

import com.chatsphere.model.PinnedChat;
import com.chatsphere.model.User;
import com.chatsphere.repository.PinnedChatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pinned")
public class PinnedChatController {

    private final PinnedChatRepository repo;

    public PinnedChatController(PinnedChatRepository repo) {
        this.repo = repo;
    }

    // GET /api/pinned  → list of my pinned chat keys
    @GetMapping
    public ResponseEntity<List<String>> getPinned(@AuthenticationPrincipal User user) {
        List<String> keys = repo.findByUsernameOrderByPinnedAtDesc(user.getUsername())
            .stream().map(PinnedChat::getChatKey).collect(Collectors.toList());
        return ResponseEntity.ok(keys);
    }

    // POST /api/pinned  → pin a chat
    // body: { chatKey: "1_3", type: "dm" }  or  { chatKey: "room_5", type: "group" }
    @PostMapping
    public ResponseEntity<?> pin(@RequestBody Map<String, String> body,
                                  @AuthenticationPrincipal User user) {
        String chatKey = body.get("chatKey");
        String type    = body.get("type");
        if (chatKey == null || type == null)
            return ResponseEntity.badRequest().body(Map.of("error", "chatKey and type required"));

        // Max 3 pinned chats — like WhatsApp
        long count = repo.findByUsernameOrderByPinnedAtDesc(user.getUsername()).size();
        if (count >= 3)
            return ResponseEntity.status(400).body(Map.of("error", "Maximum 3 chats can be pinned"));

        if (!repo.existsByUsernameAndChatKey(user.getUsername(), chatKey)) {
            PinnedChat p = new PinnedChat();
            p.setUsername(user.getUsername());
            p.setChatKey(chatKey);
            p.setType(type);
            repo.save(p);
        }
        return ResponseEntity.ok(Map.of("pinned", true, "chatKey", chatKey));
    }

    // DELETE /api/pinned/{chatKey}  → unpin a chat
    @DeleteMapping("/{chatKey}")
    @Transactional
    public ResponseEntity<?> unpin(@PathVariable String chatKey,
                                    @AuthenticationPrincipal User user) {
        repo.deleteByUsernameAndChatKey(user.getUsername(), chatKey);
        return ResponseEntity.ok(Map.of("unpinned", true, "chatKey", chatKey));
    }
}