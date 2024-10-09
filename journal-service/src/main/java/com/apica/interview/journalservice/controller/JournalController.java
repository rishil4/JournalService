package com.apica.interview.journalservice.controller;

import com.apica.interview.journalservice.model.JournalEntry;
import com.apica.interview.journalservice.service.JournalService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
public class JournalController {
    @Autowired
    private JournalService journalService;

    @GetMapping
    public ResponseEntity<List<JournalEntry>> getEntries(@RequestHeader("Authorization") String token) {
        String username = getUsernameFromToken(token);
        List<JournalEntry> entries = journalService.getEntriesForUser(username);
        return ResponseEntity.ok(entries);
    }

    private String getUsernameFromToken(String token) {
        // Remove "Bearer " prefix
        token = token.substring(7);
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor("sBv2rCk9YPpK+Kuj5MJKgXbBFO/5bdRUpEHmgXkGR4Q=".getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}