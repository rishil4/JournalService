package com.apica.interview.journalservice.service;

import com.apica.interview.journalservice.model.JournalEntry;
import com.apica.interview.journalservice.repository.JournalEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class JournalServiceTest {

    @InjectMocks
    private JournalService journalService;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEntriesForUser() {
        String username = "user1";
        JournalEntry entry1 = new JournalEntry();
        entry1.setUsername(username);
        entry1.setContent("Entry 1");
        entry1.setCreatedAt(new Date());

        JournalEntry entry2 = new JournalEntry();
        entry2.setUsername(username);
        entry2.setContent("Entry 2");
        entry2.setCreatedAt(new Date());

        when(journalEntryRepository.findByUsername(username)).thenReturn(Arrays.asList(entry1, entry2));

        List<JournalEntry> entries = journalService.getEntriesForUser(username);

        assertEquals(2, entries.size());
        assertEquals("Entry 1", entries.get(0).getContent());
        assertEquals("Entry 2", entries.get(1).getContent());
        verify(journalEntryRepository, times(1)).findByUsername(username);
    }

    @Test
    void testConsumeUserEvent() throws JsonProcessingException, ParseException {
        String event = "username='user3', content='{\"content\":\"This is my third journal entry\"}', createdAt=Tue Oct 08 19:42:59 IST 2024";
        JournalEntry expectedEntry = new JournalEntry();
        expectedEntry.setUsername("user3");
        expectedEntry.setContent("This is my third journal entry");
        expectedEntry.setCreatedAt(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse("Tue Oct 08 19:42:59 IST 2024"));

        journalService.consumeUserEvent(event);

        ArgumentCaptor<JournalEntry> argumentCaptor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, times(1)).save(argumentCaptor.capture());
        JournalEntry capturedEntry = argumentCaptor.getValue();

        assertEquals(expectedEntry.getUsername(), capturedEntry.getUsername());
        assertEquals(expectedEntry.getContent(), capturedEntry.getContent());
        assertEquals(expectedEntry.getCreatedAt(), capturedEntry.getCreatedAt());
    }

    @Test
    void testConsumeUserEvent_invalidJson() {
        String event = "username='user3', content='invalid json', createdAt=Tue Oct 08 19:42:59 IST 2024";

        assertThrows(RuntimeException.class, () -> {
            journalService.consumeUserEvent(event);
        });
    }
}
