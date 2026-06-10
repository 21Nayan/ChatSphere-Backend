package com.chatsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sender;

    @Column(nullable = false, length = 50)
    private String receiver;

    @Column(name = "dm_key", nullable = false, length = 30)
    private String dmKey;

    @Column(columnDefinition = "TEXT")
    private String content = "";

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "is_deleted_everyone")
    private boolean deletedEveryone = false;

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

    public Message() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Message m = new Message();
        public Builder sender(String v)    { m.sender = v;    return this; }
        public Builder receiver(String v)  { m.receiver = v;  return this; }
        public Builder dmKey(String v)     { m.dmKey = v;     return this; }
        public Builder content(String v)   { m.content = v != null ? v : ""; return this; }
        public Builder fileUrl(String v)   { m.fileUrl = v;   return this; }
        public Builder fileName(String v)  { m.fileName = v;  return this; }
        public Builder fileType(String v)  { m.fileType = v;  return this; }
        public Builder fileSize(Long v)    { m.fileSize = v;  return this; }
        public Builder createdAt(LocalDateTime v) { m.createdAt = v; return this; }
        public Message build()             { return m; }
    }

    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }
    public String getSender()                 { return sender; }
    public void setSender(String v)           { this.sender = v; }
    public String getReceiver()               { return receiver; }
    public void setReceiver(String v)         { this.receiver = v; }
    public String getDmKey()                  { return dmKey; }
    public void setDmKey(String v)            { this.dmKey = v; }
    public String getContent()                { return content; }
    public void setContent(String v)          { this.content = v != null ? v : ""; }
    public boolean isDeleted()                { return deleted; }
    public void setDeleted(boolean v)         { this.deleted = v; }
    public boolean isDeletedEveryone()        { return deletedEveryone; }
    public void setDeletedEveryone(boolean v) { this.deletedEveryone = v; }
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