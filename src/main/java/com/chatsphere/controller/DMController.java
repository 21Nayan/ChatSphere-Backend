package com.chatsphere.controller;

import com.chatsphere.model.User;
import com.chatsphere.repository.UserRepository;
import com.chatsphere.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dm")
public class DMController {

    private final ChatService    chatService;
    private final UserRepository userRepository;

    public DMController(ChatService chatService, UserRepository userRepository) {
        this.chatService    = chatService;
        this.userRepository = userRepository;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getDMConversations(currentUser.getUsername()));
    }

    @GetMapping("/messages/{dmKey}")
    public ResponseEntity<List<Map<String, Object>>> getMessages(
            @PathVariable String dmKey) {
        return ResponseEntity.ok(chatService.getDMMessages(dmKey));
    }

    @PostMapping("/messages/{dmKey}")
    public ResponseEntity<?> sendMessage(
            @PathVariable String dmKey,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            String content  = (String) body.get("content");
            String fileUrl  = (String) body.get("fileUrl");
            String fileName = (String) body.get("fileName");
            String fileType = (String) body.get("fileType");
            Long   fileSize = body.get("fileSize") != null
                ? Long.parseLong(body.get("fileSize").toString()) : null;

            // ── BULLETPROOF RECEIVER PARSING ──
            String receiver = "unknown";
            String[] parts  = dmKey.split("_");
            
            // If the frontend sends "3_SphereAI", this safely grabs "SphereAI"
            String otherIdStr = dmKey;
            if (parts.length == 2) {
                otherIdStr = String.valueOf(currentUser.getId()).equals(parts[0]) ? parts[1] : parts[0];
            }

            // Aggressively remove spaces and ignore case to prevent the silent freeze
            if (otherIdStr.toLowerCase().replace(" ", "").contains("sphereai")) {
                receiver = "SphereAI";
            } else {
                try {
                    Long otherId = Long.parseLong(otherIdStr);
                    receiver = userRepository.findById(otherId)
                        .map(User::getUsername).orElse("unknown");
                } catch (NumberFormatException e) {
                    receiver = otherIdStr; // Fallback
                }
            }

            return ResponseEntity.ok(chatService.saveDMAndBroadcast(
                dmKey, content, currentUser.getUsername(), receiver,
                fileUrl, fileName, fileType, fileSize));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to send message"));
        }
    }

    @DeleteMapping("/messages/{msgId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long msgId,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.deleteDMMessage(msgId, currentUser.getUsername());
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete"));
        }
    }
}