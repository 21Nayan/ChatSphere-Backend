package com.chatsphere.config;

import com.chatsphere.model.User;
import com.chatsphere.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("hardik.dev",      "Hardik",   "hardik@chatsphere.io",  "Full Stack Dev");
        seedUser("nayan.ui",        "Nayan",    "nayan@chatsphere.io",   "UI Designer");
        seedUser("karan.backend",   "Karan",    "karan@chatsphere.io",   "Backend Dev");
        seedUser("gaurav.pm",       "Gaurav",   "gaurav@chatsphere.io",  "Product Manager");
    }

    private void seedUser(String username, String displayName, String email, String role) {
        if (!userRepository.existsByUsername(username)) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("password"));
            u.setEmail(email);
            u.setDisplayName(displayName);
            u.setRole(role);
            u.setOnline(false);
            userRepository.save(u);
            System.out.println("Seeded user: " + username);
        }
    }
}