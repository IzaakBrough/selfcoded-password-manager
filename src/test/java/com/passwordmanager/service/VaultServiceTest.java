package com.passwordmanager.service;

import com.passwordmanager.model.PasswordEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VaultServiceTest {

    private static final String TEST_PASSWORD = "TestMasterPassword123!";
    
    @AfterEach
    void cleanup() throws IOException {
        // Clean up test files
        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, ".passwordmanager");
        if (Files.exists(appDir)) {
            Files.walk(appDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @Test
    void testCreateAndUnlockVault() throws Exception {
        VaultService service = new VaultService();
        
        // Initially no vault should exist
        assertFalse(service.vaultExists());
        
        // Create vault
        service.createVault(TEST_PASSWORD);
        
        // Now vault should exist
        assertTrue(service.vaultExists());
        
        // Unlock with correct password
        assertTrue(service.unlockVault(TEST_PASSWORD));
        
        // Should start with empty entries
        List<PasswordEntry> entries = service.getEntries();
        assertNotNull(entries);
        assertEquals(0, entries.size());
    }

    @Test
    void testAddAndRetrieveEntry() throws Exception {
        VaultService service = new VaultService();
        service.createVault(TEST_PASSWORD);
        
        // Add entry
        PasswordEntry entry = new PasswordEntry("example.com", "user@example.com", "password123", "Test notes");
        service.addEntry(entry);
        
        // Retrieve entries
        List<PasswordEntry> entries = service.getEntries();
        assertEquals(1, entries.size());
        
        PasswordEntry retrieved = entries.get(0);
        assertEquals("example.com", retrieved.getWebsite());
        assertEquals("user@example.com", retrieved.getUsername());
        assertEquals("password123", retrieved.getPassword());
        assertEquals("Test notes", retrieved.getNotes());
    }

    @Test
    void testSearchEntries() throws Exception {
        VaultService service = new VaultService();
        service.createVault(TEST_PASSWORD);
        
        // Add multiple entries
        service.addEntry(new PasswordEntry("google.com", "user1@gmail.com", "pass1", "Google account"));
        service.addEntry(new PasswordEntry("facebook.com", "user2@fb.com", "pass2", "Facebook account"));
        service.addEntry(new PasswordEntry("twitter.com", "user3@twitter.com", "pass3", "Twitter account"));
        
        // Search
        List<PasswordEntry> results = service.searchEntries("google");
        assertEquals(1, results.size());
        assertEquals("google.com", results.get(0).getWebsite());
        
        // Search by username
        results = service.searchEntries("user2");
        assertEquals(1, results.size());
        assertEquals("facebook.com", results.get(0).getWebsite());
    }

    @Test
    void testDeleteEntry() throws Exception {
        VaultService service = new VaultService();
        service.createVault(TEST_PASSWORD);
        
        PasswordEntry entry = new PasswordEntry("test.com", "user@test.com", "pass", "notes");
        service.addEntry(entry);
        
        assertEquals(1, service.getEntries().size());
        
        service.deleteEntry(entry);
        
        assertEquals(0, service.getEntries().size());
    }

    @Test
    void testWrongPassword() throws Exception {
        VaultService service = new VaultService();
        service.createVault(TEST_PASSWORD);
        
        // Try to unlock with wrong password
        assertFalse(service.unlockVault("WrongPassword123!"));
    }

    @Test
    void testPersistence() throws Exception {
        // Create vault and add entry
        VaultService service1 = new VaultService();
        service1.createVault(TEST_PASSWORD);
        service1.addEntry(new PasswordEntry("test.com", "user", "pass", "notes"));
        
        // Create new service instance and unlock
        VaultService service2 = new VaultService();
        assertTrue(service2.unlockVault(TEST_PASSWORD));
        
        // Should retrieve the saved entry
        List<PasswordEntry> entries = service2.getEntries();
        assertEquals(1, entries.size());
        assertEquals("test.com", entries.get(0).getWebsite());
    }
}
