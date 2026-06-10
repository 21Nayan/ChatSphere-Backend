package com.chatsphere.repository;

import com.chatsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ── CASE SENSITIVE (Standard) ──
    Optional<User> findByUsername(String username);
    
    // ── CASE INSENSITIVE (🌟 Required for Recovery Fix) ──
    Optional<User> findByUsernameIgnoreCase(String username);
    
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsOnlineTrue();
}