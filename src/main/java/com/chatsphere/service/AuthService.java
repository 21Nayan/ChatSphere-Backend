package com.chatsphere.service;

import com.chatsphere.model.User;
import com.chatsphere.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    // ── Login Protocol ─────────────────────────────────────────────
    public Map<String, Object> login(String username, String password) {
        String cleanUsername = username != null ? username.trim() : "";
        
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(cleanUsername, password)
        );

        User user = userRepository.findByUsernameIgnoreCase(cleanUsername)
                .orElseThrow(() -> new RuntimeException("Identity not found in system."));

        user.setOnline(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",          user.getId());
        userMap.put("username",    user.getUsername());
        userMap.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        userMap.put("role",        user.getRole() != null ? user.getRole() : "Member");
        userMap.put("email",       user.getEmail());
        userMap.put("phone",       user.getPhone());
        userMap.put("caption",     user.getCaption());
        userMap.put("avatar",      user.getAvatar());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user",  userMap);
        return response;
    }

    // ── Registration Protocol ──────────────────────────────────────
    @Transactional
    public Map<String, Object> register(String username, String password, String email) {
        String cleanUsername = username != null ? username.trim() : "";
        
        if (userRepository.existsByUsername(cleanUsername))
            throw new RuntimeException("Username identity already claimed.");
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email))
            throw new RuntimeException("Digital address already associated with an identity.");

        User user = User.builder()
                .username(cleanUsername)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role("Member")
                .caption("Hey there! I am using ChatSphere 🔥")
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",          user.getId());
        userMap.put("username",    user.getUsername());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("role",        user.getRole());
        userMap.put("email",       user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user",  userMap);
        return response;
    }

    // ── Profile Update Protocol ────────────────────────────────────
    @Transactional
    public User updateProfile(Long userId, Map<String, String> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("displayName")) user.setDisplayName(updates.get("displayName"));
        if (updates.containsKey("caption"))     user.setCaption(updates.get("caption"));
        if (updates.containsKey("phone"))       user.setPhone(updates.get("phone"));
        if (updates.containsKey("role"))        user.setRole(updates.get("role"));
        if (updates.containsKey("email"))       user.setEmail(updates.get("email"));
        if (updates.containsKey("avatar"))      user.setAvatar(updates.get("avatar"));

        return userRepository.save(user);
    }

    // ── RECOVERY: Request Security Code (OTP) ──────────────────────
    @Transactional
    public void requestOtpByUsername(String username) {
        String cleanUsername = username != null ? username.trim() : "";
        
        // 🔍 Telemetry: Printing the exact input being queried
        System.out.println("LOG: Recovery protocol initiated for: [" + cleanUsername + "]");
        
        User user = userRepository.findByUsernameIgnoreCase(cleanUsername)
                .orElseThrow(() -> {
                    System.err.println("LOG: FAILED to find identifier: [" + cleanUsername + "]");
                    return new RuntimeException("Identity identifier not found.");
                });

        System.out.println("LOG: Identity verified. Sending OTP to: " + user.getEmail());

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("No digital address linked to this identity.");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    // ── RECOVERY: Identity Verification & Reset ────────────────────
    @Transactional
    public void resetPasswordByUsername(String username, String otp, String newPassword) {
        String cleanUsername = username != null ? username.trim() : "";

        User user = userRepository.findByUsernameIgnoreCase(cleanUsername)
                .orElseThrow(() -> new RuntimeException("System identity verification failed."));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid security artifact (OTP). Access denied.");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Security artifact has expired. Protocol timeout.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        
        userRepository.save(user);
    }
}