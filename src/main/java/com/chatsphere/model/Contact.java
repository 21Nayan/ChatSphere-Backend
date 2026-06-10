package com.chatsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"owner_name", "phone"}))
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── owner username (not ID) ───────────────────────────────
    @Column(name = "owner_name", nullable = false, length = 50)
    private String ownerName;

    @Column(nullable = false, length = 100)
    private String name;         // contact display name

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String label;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──────────────────────────────────────────
    public Contact() {}

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }
    public String getOwnerName()              { return ownerName; }
    public void setOwnerName(String v)        { this.ownerName = v; }
    public String getName()                   { return name; }
    public void setName(String v)             { this.name = v; }
    public String getPhone()                  { return phone; }
    public void setPhone(String v)            { this.phone = v; }
    public String getLabel()                  { return label; }
    public void setLabel(String v)            { this.label = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}