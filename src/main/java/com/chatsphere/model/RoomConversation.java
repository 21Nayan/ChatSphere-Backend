package com.chatsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_conversations")
public class RoomConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(nullable = false, length = 50)
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String content = "";

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "is_pinned")
    private boolean pinned = false;

    @Column(name = "file_url",  length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RoomConversation() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final RoomConversation r = new RoomConversation();
        public Builder roomId(Long v)     { r.roomId = v;    return this; }
        public Builder sender(String v)   { r.sender = v;    return this; }
        public Builder content(String v)  { r.content = v != null ? v : ""; return this; }
        public Builder fileUrl(String v)  { r.fileUrl = v;   return this; }
        public Builder fileName(String v) { r.fileName = v;  return this; }
        public Builder fileType(String v) { r.fileType = v;  return this; }
        public Builder fileSize(Long v)   { r.fileSize = v;  return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public RoomConversation build()   { return r; }
    }

    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }
    public Long getRoomId()                   { return roomId; }
    public void setRoomId(Long v)             { this.roomId = v; }
    public String getSender()                 { return sender; }
    public void setSender(String v)           { this.sender = v; }
    public String getContent()                { return content; }
    public void setContent(String v)          { this.content = v != null ? v : ""; }
    public boolean isDeleted()                { return deleted; }
    public void setDeleted(boolean v)         { this.deleted = v; }
    public boolean isPinned()                 { return pinned; }
    public void setPinned(boolean v)          { this.pinned = v; }
    public String getFileUrl()                { return fileUrl; }
    public void setFileUrl(String v)          { this.fileUrl = v; }
    public String getFileName()               { return fileName; }
    public void setFileName(String v)         { this.fileName = v; }
    public String getFileType()               { return fileType; }
    public void setFileType(String v)         { this.fileType = v; }
    public Long getFileSize()                 { return fileSize; }
    public void setFileSize(Long v)           { this.fileSize = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}