package com.chatsphere.controller;

import com.chatsphere.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Protocol: User Authentication
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> response = authService.login(
                body.get("username"),
                body.get("password")
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid username identifier or password."));
        }
    }

    /**
     * Protocol: Identity Initialization
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> response = authService.register(
                body.get("username"),
                body.get("password"),
                body.get("email") 
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Protocol: Security Key Dispatch (Via Username)
     * POST /api/auth/forgot-password
     * Payload: { "username": "nayan.ui" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            authService.requestOtpByUsername(username);
            return ResponseEntity.ok(Map.of(
                "message", "Security artifact dispatched to the digital address registered with this identity."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Protocol: Identity Verification & Access Key Update
     * POST /api/auth/reset-password
     * Payload: { "username": "...", "otp": "...", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            authService.resetPasswordByUsername(
                body.get("username"), 
                body.get("otp"), 
                body.get("newPassword")
            );
            return ResponseEntity.ok(Map.of(
                "message", "System access key authorized and updated. Identity verified."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}