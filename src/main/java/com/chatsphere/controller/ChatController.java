package com.chatsphere.controller;

import com.chatsphere.model.User;
import com.chatsphere.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Value("${upload.base-dir:C:/Users/nayan/eclipse-workspace/ChatsphereBackend/uploads}")
    private String baseDir;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ── Groups ────────────────────────────────────────────────
    @GetMapping("/groups")
    public ResponseEntity<List<Map<String, Object>>> getMyGroups(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getGroupsForUser(currentUser.getUsername()));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        try { return ResponseEntity.ok(chatService.getRoom(id)); }
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("error", "Room not found")); }
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            String name        = (String) body.get("name");
            String description = (String) body.getOrDefault("description", "");
            String type        = (String) body.getOrDefault("type", "group");
            List<String> memberUsernames = List.of();
            if (body.containsKey("memberUsernames") && body.get("memberUsernames") != null) {
                memberUsernames = ((List<?>) body.get("memberUsernames")).stream()
                    .map(Object::toString).collect(Collectors.toList());
            }
            return ResponseEntity.ok(chatService.createRoom(name, description, type,
                currentUser.getUsername(), memberUsernames));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed"));
        }
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id, @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        try { return ResponseEntity.ok(chatService.updateRoom(id, body.get("name"), currentUser.getUsername())); }
        catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    // ── NEW: Group Profile Image Upload ──────────────────────
    @PostMapping("/rooms/{id}/image")
    public ResponseEntity<?> uploadGroupImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));

        try {
            // Path: baseDir/groups/{id}/
            Path folder = Paths.get(baseDir, "groups", id.toString()).toAbsolutePath().normalize();
            Files.createDirectories(folder);

            String original  = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String extension = original.contains(".") ? original.substring(original.lastIndexOf(".")) : "";
            String filename  = "group_avatar_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Files.copy(file.getInputStream(), folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            // Frontend path to reach serveFile: /api/chat/files/groups/{id}/{filename}
            String imageUrl = "/api/chat/files/groups/" + id + "/" + filename;

            // Update database via service
            Map<String, Object> updatedRoom = chatService.updateRoomImage(id, imageUrl, currentUser.getUsername());

            return ResponseEntity.ok(Map.of(
                "imageUrl", imageUrl,
                "room", updatedRoom
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ── Admins ────────────────────────────────────────────────
    @PostMapping("/rooms/{id}/admins/{username}")
    public ResponseEntity<?> addAdmin(
            @PathVariable Long id, @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.addAdmin(id, username, currentUser.getUsername());
            return ResponseEntity.ok(chatService.getRoom(id));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/rooms/{id}/admins/{username}")
    public ResponseEntity<?> removeAdmin(
            @PathVariable Long id, @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.removeAdmin(id, username, currentUser.getUsername());
            return ResponseEntity.ok(chatService.getRoom(id));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    // ── Members ───────────────────────────────────────────────
    @PostMapping("/rooms/{id}/members/{username}")
    public ResponseEntity<?> addMember(
            @PathVariable Long id, @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.addMember(id, username, currentUser.getUsername());
            return ResponseEntity.ok(chatService.getRoom(id));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/rooms/{id}/members/{username}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long id, @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.removeMember(id, username, currentUser.getUsername());
            return ResponseEntity.ok(chatService.getRoom(id));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    // ── Room Messages ─────────────────────────────────────────
    @GetMapping("/rooms/{id}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getRoomMessages(id));
    }

    @PostMapping("/rooms/{id}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long id, @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            String content   = (String) body.get("content");
            String fileUrl   = (String) body.get("fileUrl");
            String fileName = (String) body.get("fileName");
            String fileType = (String) body.get("fileType");
            Long   fileSize = body.get("fileSize") != null
                ? Long.parseLong(body.get("fileSize").toString()) : null;
            return ResponseEntity.ok(chatService.saveAndBroadcast(
                id, content, currentUser.getUsername(),
                fileUrl, fileName, fileType, fileSize));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/messages/{msgId}")
    public ResponseEntity<?> deleteRoomMessage(
            @PathVariable Long msgId,
            @AuthenticationPrincipal User currentUser) {
        try {
            chatService.deleteRoomMessage(msgId, currentUser.getUsername());
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); }
    }

    // ── Users ─────────────────────────────────────────────────
    @GetMapping("/users/online")
    public ResponseEntity<List<Map<String, Object>>> getOnlineUsers() {
        return ResponseEntity.ok(chatService.getOnlineUsers());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(chatService.getAllUsers());
    }

    // ── File Upload (General) ──────────────────────────────────
    @PostMapping("/files/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file")     MultipartFile file,
            @RequestParam("context")  String context,
            @RequestParam("threadId") String threadId,
            @AuthenticationPrincipal User currentUser) {

        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        
        try {
            Path folder = Paths.get(baseDir, context, threadId).toAbsolutePath().normalize();
            Files.createDirectories(folder);

            String original  = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String safe      = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uid       = UUID.randomUUID().toString().substring(0, 8);
            String filename  = timestamp + "_" + uid + "_" + safe;

            Files.copy(file.getInputStream(), folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/chat/files/" + context + "/" + threadId + "/" + filename;

            return ResponseEntity.ok(Map.of(
                "url",         fileUrl,
                "fileName",    original,
                "size",        file.getSize(),
                "contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            ));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ── File Serve ────────────────────────────────────────────
    @GetMapping("/files/{context}/{threadId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String context,
            @PathVariable String threadId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(baseDir, context, threadId, filename).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) return ResponseEntity.notFound().build();

            String contentType;
            try   { contentType = Files.probeContentType(filePath); }
            catch (IOException e) { contentType = null; }
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}