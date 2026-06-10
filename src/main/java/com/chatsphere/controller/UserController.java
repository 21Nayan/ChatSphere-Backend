package com.chatsphere.controller;

import com.chatsphere.model.Contact;
import com.chatsphere.model.User;
import com.chatsphere.repository.ContactRepository;
import com.chatsphere.repository.UserRepository;
import com.chatsphere.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository    userRepository;
    private final ContactRepository contactRepository;
    private final ChatService       chatService;

    public UserController(UserRepository userRepository,
                          ContactRepository contactRepository,
                          ChatService chatService) {
        this.userRepository    = userRepository;
        this.contactRepository = contactRepository;
        this.chatService       = chatService;
    }

    // ── Profile ───────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.userToMap(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal User currentUser) {
        
        // 1. Fetch fresh user from DB using the username
        User dbUser = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Apply updates to the fresh DB User
        if (body.containsKey("displayName"))     dbUser.setDisplayName(body.get("displayName"));
        if (body.containsKey("caption"))         dbUser.setCaption(body.get("caption"));
        if (body.containsKey("phone"))           dbUser.setPhone(body.get("phone"));
        if (body.containsKey("avatar"))          dbUser.setAvatar(body.get("avatar"));
        if (body.containsKey("lastSeenPrivacy")) dbUser.setLastSeenPrivacy(body.get("lastSeenPrivacy"));
        
        // 3. Save to database
        userRepository.save(dbUser);
        
        return ResponseEntity.ok(chatService.userToMap(dbUser));
    }
    // ── Logout — save lastSeen timestamp ─────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User currentUser) {
        currentUser.setOnline(false);
        currentUser.setLastSeen(LocalDateTime.now());  // ← record when they went offline
        userRepository.save(currentUser);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ── Heartbeat — called every 30s while app is open ───────
    // Keeps isOnline=true and updates lastSeen while active
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@AuthenticationPrincipal User currentUser) {
        currentUser.setOnline(true);
        currentUser.setLastSeen(LocalDateTime.now());
        userRepository.save(currentUser);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ── Get any user's last seen (respects their privacy setting)
    @GetMapping("/{username}/lastseen")
    public ResponseEntity<?> getLastSeen(@PathVariable String username,
                                          @AuthenticationPrincipal User currentUser) {
        return userRepository.findByUsername(username).map(u -> {
            // If privacy = nobody, hide it
            if ("nobody".equals(u.getLastSeenPrivacy()))
                return ResponseEntity.ok(Map.of("hidden", true));
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("username", u.getUsername());
            result.put("isOnline", u.isOnline());
            result.put("lastSeen", u.getLastSeen());
            result.put("hidden",   false);
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Contacts ──────────────────────────────────────────────
    @GetMapping("/contacts")
    public ResponseEntity<List<Contact>> getContacts(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactRepository.findByOwnerName(currentUser.getUsername()));
    }

    @PostMapping("/contacts")
    public ResponseEntity<?> addContact(@RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal User currentUser) {
        String phone = body.get("phone");
        if (contactRepository.existsByOwnerNameAndPhone(currentUser.getUsername(), phone))
            return ResponseEntity.status(409).body(Map.of("error", "Contact with this phone already exists"));
        Contact c = new Contact();
        c.setOwnerName(currentUser.getUsername());
        c.setName(body.get("name"));
        c.setPhone(phone);
        c.setLabel(body.getOrDefault("label", ""));
        return ResponseEntity.ok(contactRepository.save(c));
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id,
                                            @AuthenticationPrincipal User currentUser) {
        return contactRepository.findById(id).map(c -> {
            if (!c.getOwnerName().equals(currentUser.getUsername()))
                return ResponseEntity.status(403).body(Map.of("error", "Not your contact"));
            contactRepository.delete(c);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Contact not found")));
    }
}