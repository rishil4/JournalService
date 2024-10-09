package com.apica.interview.journalservice.service;

import com.apica.interview.journalservice.model.JournalEntry;
import com.apica.interview.journalservice.repository.JournalEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class JournalService {

    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "user-events", groupId = "journal-group")
    public void consumeUserEvent(String event) throws JsonProcessingException, ParseException {
        JournalEntry entry = parseJournalEntryFromEvent(event);
        journalEntryRepository.save(entry);
        System.out.println("Journal entry created: " + entry);
    }

    private JournalEntry parseJournalEntryFromEvent(String event) throws JsonProcessingException, ParseException {
        String username = extractField(event, "username");
        String contentJsonString = extractField(event, "content");
        Date createdAt = extractDateField(event, "createdAt");

        String content = extractContentFromJson(contentJsonString);

        JournalEntry entry = new JournalEntry();
        entry.setUsername(username);
        entry.setContent(content);
        entry.setCreatedAt(createdAt);

        return entry;
    }

    private String extractContentFromJson(String contentJsonString) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(contentJsonString);
        return jsonNode.get("content").asText();
    }

    private String extractField(String str, String fieldName) {
        int startIndex = str.indexOf(fieldName + "=") + (fieldName.length() + 1);
        int endIndex = str.indexOf(',', startIndex);
        if (endIndex == -1) {
            endIndex = str.indexOf('}', startIndex);
        }
        try {
            return str.substring(startIndex, endIndex).replace("'", "").trim();
        } catch (StringIndexOutOfBoundsException e){
            throw new RuntimeException("Could not extract JSON from content");
        }
    }

    private Date extractDateField(String str, String fieldName) throws ParseException {
        String dateStr = extractField(str, fieldName);
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.parse(dateStr);
    }

    public List<JournalEntry> getEntriesForUser(String username) {
        return journalEntryRepository.findByUsername(username);
    }
}
