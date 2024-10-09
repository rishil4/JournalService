package com.apica.interview.userservice.service;

import com.apica.interview.userservice.model.JournalEntry;
import com.apica.interview.userservice.model.User;
import com.apica.interview.userservice.repository.UserRepository;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        kafkaTemplate.send("user-events", "User created: " + savedUser.getUsername());
        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        User updatedUser = userRepository.save(existingUser);
        kafkaTemplate.send("user-events", "User updated: " + updatedUser.getUsername());
        return updatedUser;
    }

    public JournalEntry addEntry(String username, String content) {
        JournalEntry entry = new JournalEntry();
        entry.setUsername(username);
        entry.setContent(content);
        entry.setCreatedAt(new Date());
        kafkaTemplate.send("user-events", entry.toString());

        return entry;
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        kafkaTemplate.send("user-events", "User deleted: " + user.getUsername());
    }

    public String authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return generateToken(user);
        } else {
            throw new RuntimeException("Invalid password");
        }
    }

    private String generateToken(User user) {
        long expirationTime = 1000 * 60 * 60; //Last an hour
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor("sBv2rCk9YPpK+Kuj5MJKgXbBFO/5bdRUpEHmgXkGR4Q=".getBytes()))
                .compact(); //Randomly generated a token from an external program, used the same in the journal service as well.
    }
}