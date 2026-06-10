package com.chatsphere.service;

import com.chatsphere.model.Message;
import com.chatsphere.model.Room;
import com.chatsphere.model.RoomConversation;
import com.chatsphere.model.User;
import com.chatsphere.repository.MessageRepository;
import com.chatsphere.repository.RoomConversationRepository;
import com.chatsphere.repository.RoomRepository;
import com.chatsphere.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final RoomRepository             roomRepository;
    private final RoomConversationRepository convRepository;
    private final MessageRepository          messageRepository;
    private final UserRepository             userRepository;
    private final SimpMessagingTemplate      messagingTemplate;
    private final AiService                  aiService; 

    public ChatService(RoomRepository roomRepository,
                       RoomConversationRepository convRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate,
                       AiService aiService) {
        this.roomRepository    = roomRepository;
        this.convRepository    = convRepository;
        this.messageRepository = messageRepository;
        this.userRepository    = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.aiService         = aiService;
    }

    // ── Get single room ───────────────────────────────────────
    public Map<String, Object> getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        return roomToMap(room);
    }

    // ── Get groups for user ───────────────────────────────────
    public List<Map<String, Object>> getGroupsForUser(String username) {
        return roomRepository.findGroupsByMemberUsername(username).stream()
            .map(this::roomToMap)
            .collect(Collectors.toList());
    }

    // ── Create group ──────────────────────────────────────────
    @Transactional
    public Map<String, Object> createRoom(String name, String description,
                                          String type, String createdByUsername,
                                          List<String> memberUsernames) {
        List<String> members = new ArrayList<>();
        members.add(createdByUsername);
        if (memberUsernames != null)
            memberUsernames.stream()
                .filter(u -> u != null && !u.equals(createdByUsername))
                .forEach(members::add);

        Room room = new Room();
        room.setName(name);
        room.setDescription(description != null ? description : "");
        room.setType(type != null ? type : "group");
        room.setCreatedBy(createdByUsername);
        room.setAdminIds(createdByUsername);
        room.setMemberIds(String.join(",", members));
        room.setCreatedAt(LocalDateTime.now());

        return roomToMap(roomRepository.saveAndFlush(room));
    }

    // ── Rename group ──────────────────────────────────────────
    @Transactional
    public Map<String, Object> updateRoom(Long roomId, String newName, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getAdminList().contains(requesterUsername))
            throw new RuntimeException("Only admins can rename the group");
        room.setName(newName);
        return roomToMap(roomRepository.save(room));
    }

    // ── Add admin ─────────────────────────────────────────────
    @Transactional
    public void addAdmin(Long roomId, String username, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getAdminList().contains(requesterUsername))
            throw new RuntimeException("Only admins can add other admins");
        List<String> admins = room.getAdminList();
        if (!admins.contains(username)) {
            admins.add(username);
            room.setAdminList(admins);
            roomRepository.save(room);
        }
    }

    // ── Remove admin ──────────────────────────────────────────
    @Transactional
    public void removeAdmin(Long roomId, String username, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getAdminList().contains(requesterUsername))
            throw new RuntimeException("Only admins can remove other admins");
        List<String> admins = room.getAdminList();
        admins.remove(username);
        room.setAdminList(admins);
        roomRepository.save(room);
    }

    // ── Remove member ─────────────────────────────────────────
    @Transactional
    public void removeMember(Long roomId, String username, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getAdminList().contains(requesterUsername))
            throw new RuntimeException("Only admins can remove members");
        List<String> members = room.getMemberList();
        members.remove(username);
        room.setMemberList(members);
        List<String> admins = room.getAdminList();
        admins.remove(username);
        room.setAdminList(admins);
        roomRepository.save(room);
    }

    // ── Add member ────────────────────────────────────────────
    @Transactional
    public void addMember(Long roomId, String username, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getAdminList().contains(requesterUsername))
            throw new RuntimeException("Only admins can add members");
        List<String> members = room.getMemberList();
        if (!members.contains(username)) {
            members.add(username);
            room.setMemberList(members);
            roomRepository.save(room);
        }
    }

    // ── Room messages ─────────────────────────────────────────
    public List<Map<String, Object>> getRoomMessages(Long roomId) {
        PageRequest page = PageRequest.of(0, 50);
        return convRepository.findByRoomIdOrderByCreatedAtAsc(roomId, page).stream()
            .map(this::convToMap)
            .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> saveAndBroadcast(Long roomId, String content, String sender,
                                                String fileUrl, String fileName,
                                                String fileType, Long fileSize) {
        RoomConversation msg = RoomConversation.builder()
            .roomId(roomId).sender(sender).content(content != null ? content : "")
            .fileUrl(fileUrl).fileName(fileName).fileType(fileType).fileSize(fileSize)
            .createdAt(LocalDateTime.now()).build();
        RoomConversation saved = convRepository.save(msg);
        Map<String, Object> map = convToMap(saved);
        messagingTemplate.convertAndSend("/topic/room." + roomId, map);
        return map;
    }

    @Transactional
    public void deleteRoomMessage(Long msgId, String requesterUsername) {
        RoomConversation msg = convRepository.findById(msgId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!msg.getSender().equals(requesterUsername))
            throw new RuntimeException("Can only delete your own messages");
        convRepository.deleteById(msgId);
    }

    // ── DM messages ───────────────────────────────────────────
    public List<Map<String, Object>> getDMConversations(String username) {
        List<Message> all = messageRepository.findBySenderOrReceiver(username);
        Map<String, Message> latest = new LinkedHashMap<>();
        for (Message m : all)
            latest.merge(m.getDmKey(), m, (ex, nw) ->
                nw.getCreatedAt().isAfter(ex.getCreatedAt()) ? nw : ex);

        return latest.values().stream().map(m -> {
            String other = m.getSender().equals(username) ? m.getReceiver() : m.getSender();
            User otherUser = userRepository.findByUsername(other).orElse(null);
            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("dmKey",       m.getDmKey());
            conv.put("lastMessage", m.isDeleted() ? "This message was deleted"
                : (m.getContent() != null ? m.getContent() : "📎 Attachment"));
            conv.put("lastTime",    m.getCreatedAt());
            conv.put("otherUser",   otherUser != null ? userToMap(otherUser)
                : Map.of("username", other));
            return conv;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getDMMessages(String dmKey) {
        PageRequest page = PageRequest.of(0, 50);
        return messageRepository.findByDmKeyOrderByCreatedAtAsc(dmKey, page).stream()
            .map(this::messageToMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> saveDMAndBroadcast(String dmKey, String content,
                                                  String sender, String receiver,
                                                  String fileUrl, String fileName,
                                                  String fileType, Long fileSize) {
        // 1. Save user's message normally
        Message msg = Message.builder()
            .dmKey(dmKey).sender(sender).receiver(receiver).content(content != null ? content : "")
            .fileUrl(fileUrl).fileName(fileName).fileType(fileType).fileSize(fileSize)
            .createdAt(LocalDateTime.now()).build();
        Message saved = messageRepository.save(msg);
        Map<String, Object> map = messageToMap(saved);
        messagingTemplate.convertAndSend("/topic/dm." + dmKey, map);

        // ── AI Interaction Protocol ──
        if ("SphereAI".equals(receiver)) {
            new Thread(() -> {
                try {
                    String aiResponse = aiService.getAiResponse(content);

                    Message aiMsg = Message.builder()
                        .dmKey(dmKey)
                        .sender("SphereAI")
                        .receiver(sender)
                        .content(aiResponse)
                        .createdAt(LocalDateTime.now())
                        .build();

                    Message aiSaved = messageRepository.save(aiMsg);
                    messagingTemplate.convertAndSend("/topic/dm." + dmKey, messageToMap(aiSaved));

                } catch (Exception e) {
                    e.printStackTrace();
                    Map<String, Object> errorMap = new LinkedHashMap<>();
                    errorMap.put("id", System.currentTimeMillis());
                    errorMap.put("dmKey", dmKey);
                    errorMap.put("sender", "SphereAI");
                    errorMap.put("content", "⚠️ Error: Unable to connect to Google GenAI. Check your API Key.");
                    errorMap.put("timestamp", LocalDateTime.now());
                    messagingTemplate.convertAndSend("/topic/dm." + dmKey, errorMap);
                }
            }).start();
        }

        return map;
    }

    @Transactional
    public void deleteDMMessage(Long msgId, String requesterUsername) {
        Message msg = messageRepository.findById(msgId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!msg.getSender().equals(requesterUsername))
            throw new RuntimeException("Can only delete your own messages");
        messageRepository.deleteById(msgId);
    }

    // ── Pin room message ──────────────────────────────────────
    @Transactional
    public Map<String, Object> pinRoomMessage(Long msgId, String requesterUsername) {
        RoomConversation msg = convRepository.findById(msgId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        // Unpin previous pinned message in same room
        convRepository.findPinnedByRoomId(msg.getRoomId())
            .ifPresent(prev -> { prev.setPinned(false); convRepository.save(prev); });

        msg.setPinned(true);
        RoomConversation saved = convRepository.save(msg);
        Map<String, Object> map = convToMap(saved);
        messagingTemplate.convertAndSend("/topic/room." + msg.getRoomId() + ".pin", map);
        return map;
    }

    @Transactional
    public void unpinRoomMessage(Long roomId, String requesterUsername) {
        convRepository.findPinnedByRoomId(roomId)
            .ifPresent(msg -> { msg.setPinned(false); convRepository.save(msg); });
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".pin", Map.of("unpinned", true));
    }

    public Map<String, Object> getPinnedRoomMessage(Long roomId) {
        return convRepository.findPinnedByRoomId(roomId)
            .map(this::convToMap).orElse(null);
    }

    // ── Pin DM message ────────────────────────────────────────
    @Transactional
    public Map<String, Object> pinDMMessage(Long msgId, String requesterUsername) {
        Message msg = messageRepository.findById(msgId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        messageRepository.findPinnedByDmKey(msg.getDmKey())
            .ifPresent(prev -> { prev.setPinned(false); messageRepository.save(prev); });

        msg.setPinned(true);
        Message saved = messageRepository.save(msg);
        Map<String, Object> map = messageToMap(saved);
        messagingTemplate.convertAndSend("/topic/dm." + msg.getDmKey() + ".pin", map);
        return map;
    }

    @Transactional
    public void unpinDMMessage(String dmKey, String requesterUsername) {
        messageRepository.findPinnedByDmKey(dmKey)
            .ifPresent(msg -> { msg.setPinned(false); messageRepository.save(msg); });
        messagingTemplate.convertAndSend("/topic/dm." + dmKey + ".pin", Map.of("unpinned", true));
    }

    public Map<String, Object> getPinnedDMMessage(String dmKey) {
        return messageRepository.findPinnedByDmKey(dmKey)
            .map(this::messageToMap).orElse(null);
    }

    
    public List<Map<String, Object>> getOnlineUsers() {
        return userRepository.findByIsOnlineTrue().stream()
            .map(this::userToMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::userToMap).collect(Collectors.toList());
    }
    public Map<String, Object> updateRoomImage(Long roomId, String imageUrl, String requesterUsername) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getAdminList().contains(requesterUsername)) {
            throw new RuntimeException("Only admins can update the group profile picture");
        }

        room.setImageUrl(imageUrl);
        Room saved = roomRepository.save(room);
        
        Map<String, Object> map = roomToMap(saved);
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".info", map);
        
        return map;
    }

    // ── Helpers ───────────────────────────────────────────────
    public Map<String, Object> roomToMap(Room r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",          r.getId());
        map.put("name",        r.getName());
        map.put("description", r.getDescription() != null ? r.getDescription() : "");
        map.put("type",        r.getType());
        map.put("createdBy",   r.getCreatedBy());
        map.put("imageUrl",    r.getImageUrl());
        List<String> adminList  = r.getAdminList();
        List<String> memberList = r.getMemberList();
        map.put("adminIds",    adminList);
        map.put("members",     memberList);
        map.put("memberCount", memberList.size());
        return map;
    }

    public Map<String, Object> convToMap(RoomConversation m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",        m.getId());
        map.put("roomId",    m.getRoomId());
        map.put("sender",    m.getSender());
        map.put("content",   m.isDeleted() ? null : m.getContent());
        map.put("deleted",   m.isDeleted());
        map.put("timestamp", m.getCreatedAt());
        map.put("pinned",    m.isPinned());
        map.put("fileUrl",   m.getFileUrl());
        map.put("fileName",  m.getFileName());
        map.put("fileType",  m.getFileType());
        map.put("fileSize",  m.getFileSize());
        return map;
    }

    public Map<String, Object> messageToMap(Message m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",        m.getId());
        map.put("dmKey",     m.getDmKey());
        map.put("sender",    m.getSender());
        map.put("receiver",  m.getReceiver());
        map.put("content",   (m.isDeleted() || m.isDeletedEveryone()) ? null : m.getContent());
        map.put("deleted",   m.isDeleted() || m.isDeletedEveryone());
        map.put("timestamp", m.getCreatedAt());
        map.put("pinned",    m.isPinned());
        map.put("fileUrl",   m.getFileUrl());
        map.put("fileName",  m.getFileName());
        map.put("fileType",  m.getFileType());
        map.put("fileSize",  m.getFileSize());
        return map;
    }

    public Map<String, Object> userToMap(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",               u.getId());
        map.put("username",         u.getUsername());
        map.put("displayName",      u.getDisplayName() != null ? u.getDisplayName() : u.getUsername());
        map.put("role",             u.getRole());
        map.put("email",            u.getEmail());
        map.put("phone",            u.getPhone());
        map.put("caption",          u.getCaption());
        map.put("avatar",           u.getAvatar());
        map.put("isOnline",         u.isOnline());
        map.put("lastSeenPrivacy",  u.getLastSeenPrivacy() != null ? u.getLastSeenPrivacy() : "everyone");
        if (!"nobody".equals(u.getLastSeenPrivacy())) {
            map.put("lastSeen", u.getLastSeen());
        } else {
            map.put("lastSeen", null);
        }
        return map;
    }
}