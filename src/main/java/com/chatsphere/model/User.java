package com.chatsphere.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String role;

    @Column(length = 200)
    private String caption;

    // Fixed: Now supports large Base64 image strings
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String avatar;

    @Column(name = "is_online")
    private boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "last_seen_privacy", length = 20)
    private String lastSeenPrivacy = "everyone";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    public User() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id;
        private String username, password, displayName, email;
        private String phone, role, caption, avatar;
        private boolean isOnline = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long v)            { this.id = v;          return this; }
        public Builder username(String v)    { this.username = v;    return this; }
        public Builder password(String v)    { this.password = v;    return this; }
        public Builder displayName(String v) { this.displayName = v; return this; }
        public Builder email(String v)       { this.email = v;       return this; }
        public Builder phone(String v)       { this.phone = v;       return this; }
        public Builder role(String v)        { this.role = v;        return this; }
        public Builder caption(String v)     { this.caption = v;     return this; }
        public Builder avatar(String v)      { this.avatar = v;      return this; }
        public Builder isOnline(boolean v)   { this.isOnline = v;    return this; }

        public User build() {
            User u = new User();
            u.id = id; u.username = username; u.password = password;
            u.displayName = displayName; u.email = email; u.phone = phone;
            u.role = role; u.caption = caption; u.avatar = avatar;
            u.isOnline = isOnline; u.createdAt = createdAt;
            return u;
        }
    }

    public Long getId()                               { return id; }
    public void setId(Long v)                         { this.id = v; }
    public String getUsername()                       { return username; }
    public void setUsername(String v)                 { this.username = v; }
    public String getPassword()                       { return password; }
    public void setPassword(String v)                 { this.password = v; }
    public String getDisplayName()                    { return displayName; }
    public void setDisplayName(String v)              { this.displayName = v; }
    public String getEmail()                          { return email; }
    public void setEmail(String v)                    { this.email = v; }
    public String getPhone()                          { return phone; }
    public void setPhone(String v)                    { this.phone = v; }
    public String getRole()                           { return role; }
    public void setRole(String v)                     { this.role = v; }
    public String getCaption()                        { return caption; }
    public void setCaption(String v)                  { this.caption = v; }
    public String getAvatar()                         { return avatar; }
    public void setAvatar(String v)                   { this.avatar = v; }
    public boolean isOnline()                         { return isOnline; }
    public void setOnline(boolean v)                  { this.isOnline = v; }
    public LocalDateTime getLastSeen()                { return lastSeen; }
    public void setLastSeen(LocalDateTime v)          { this.lastSeen = v; }
    public String getLastSeenPrivacy()                { return lastSeenPrivacy; }
    public void setLastSeenPrivacy(String v)          { this.lastSeenPrivacy = v; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime v)         { this.createdAt = v; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}