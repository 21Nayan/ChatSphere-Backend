package com.chatsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pinned_chats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "chat_key"}))
public class PinnedChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who pinned it
    @Column(nullable = false, length = 50)
    private String username;

    // Unique key — dmKey for DMs (e.g. "1_3"), roomId string for groups (e.g. "room_5")
    @Column(name = "chat_key", nullable = false, length = 50)
    private String chatKey;

    // "dm" or "group"
    @Column(nullable = false, length = 10)
    private String type;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt = LocalDateTime.now();

    public PinnedChat() {}

    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }
    public String getUsername()               { return username; }
    public void setUsername(String v)         { this.username = v; }
    public String getChatKey()                { return chatKey; }
    public void setChatKey(String v)          { this.chatKey = v; }
    public String getType()                   { return type; }
    public void setType(String v)             { this.type = v; }
    public LocalDateTime getPinnedAt()        { return pinnedAt; }
    public void setPinnedAt(LocalDateTime v)  { this.pinnedAt = v; }
}