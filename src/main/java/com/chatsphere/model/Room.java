package com.chatsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String type = "group";

    // 🌟 Added: Path to the group profile picture
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "admin_ids", length = 1000, nullable = false)
    private String adminIds = "";

    @Column(name = "member_ids", length = 1000, nullable = false)
    private String memberIds = "";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void sanitize() {
        if (adminIds  == null) adminIds  = "";
        if (memberIds == null) memberIds = "";
        if (createdAt == null) createdAt = LocalDateTime.now();
        // Ensure imageUrl isn't a blank string if not provided
        if (imageUrl != null && imageUrl.isBlank()) imageUrl = null;
    }

    public Room() {}

    // ── Builder (Updated with imageUrl) ──────────────────────
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Room r = new Room();
        public Builder name(String v)        { r.name = v;        return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder type(String v)        { r.type = v;        return this; }
        public Builder imageUrl(String v)    { r.imageUrl = v;    return this; } // Added to builder
        public Builder createdBy(String v)   { r.createdBy = v;   return this; }
        public Builder adminIds(String v)    { r.adminIds = v;    return this; }
        public Builder memberIds(String v)   { r.memberIds = v;   return this; }
        public Room build()                  { return r; }
    }

    // ── Helpers ───────────────────────────────────────────────
    public java.util.List<String> getAdminList() {
        if (adminIds == null || adminIds.isBlank()) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(
            java.util.Arrays.stream(adminIds.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList())
        );
    }
    public void setAdminList(java.util.List<String> list) {
        this.adminIds = (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }

    public java.util.List<String> getMemberList() {
        if (memberIds == null || memberIds.isBlank()) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(
            java.util.Arrays.stream(memberIds.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList())
        );
    }
    public void setMemberList(java.util.List<String> list) {
        this.memberIds = (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }

    // ── Getters & Setters (Updated) ───────────────────────────
    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }
    public String getName()                   { return name; }
    public void setName(String v)             { this.name = v; }
    public String getDescription()            { return description; }
    public void setDescription(String v)      { this.description = v; }
    public String getType()                   { return type; }
    public void setType(String v)             { this.type = v; }
    
    public String getImageUrl()               { return imageUrl; } // Added getter
    public void setImageUrl(String v)         { this.imageUrl = v; } // Added setter

    public String getCreatedBy()              { return createdBy; }
    public void setCreatedBy(String v)        { this.createdBy = v; }
    public String getAdminIds()               { return adminIds; }
    public void setAdminIds(String v)         { this.adminIds = v; }
    public String getMemberIds()              { return memberIds; }
    public void setMemberIds(String v)        { this.memberIds = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}