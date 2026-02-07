package com.passwordmanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.passwordmanager.model.PasswordEntry;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VaultService {
    private static final String APP_DIR = ".passwordmanager";
    private static final String VAULT_FILE = "vault.enc";
    private static final String MASTER_HASH_FILE = "master.hash";
    private static final int BCRYPT_STRENGTH = 12;

    private final Path appDirectory;
    private final Path vaultPath;
    private final Path masterHashPath;
    private final EncryptionService encryptionService;
    private final Gson gson;

    private List<PasswordEntry> entries;
    private String masterPassword;

    public VaultService() {
        String userHome = System.getProperty("user.home");
        this.appDirectory = Paths.get(userHome, APP_DIR);
        this.vaultPath = appDirectory.resolve(VAULT_FILE);
        this.masterHashPath = appDirectory.resolve(MASTER_HASH_FILE);
        this.encryptionService = new EncryptionService();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        this.entries = new ArrayList<>();
    }

    /**
     * Initialize the application directory
     */
    private void initializeAppDirectory() throws IOException {
        if (!Files.exists(appDirectory)) {
            Files.createDirectories(appDirectory);
            // Set directory permissions (Unix-like systems)
            File dir = appDirectory.toFile();
            dir.setReadable(true, true);
            dir.setWritable(true, true);
            dir.setExecutable(true, true);
        }
    }

    /**
     * Check if a vault exists
     */
    public boolean vaultExists() {
        return Files.exists(masterHashPath);
    }

    /**
     * Create a new vault with master password
     */
    public void createVault(String password) throws Exception {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Master password must be at least 8 characters");
        }

        initializeAppDirectory();

        // Hash master password with BCrypt
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_STRENGTH));

        // Save hash to file
        Files.writeString(masterHashPath, hash);

        // Initialize empty vault
        this.masterPassword = password;
        this.entries = new ArrayList<>();
        saveVault();
    }

    /**
     * Verify master password and unlock vault
     */
    public boolean unlockVault(String password) throws Exception {
        if (!vaultExists()) {
            throw new IllegalStateException("No vault exists");
        }

        // Read stored hash
        String storedHash = Files.readString(masterHashPath);

        // Verify password
        if (BCrypt.checkpw(password, storedHash)) {
            this.masterPassword = password;
            loadVault();
            return true;
        }

        return false;
    }

    /**
     * Load vault from encrypted file
     */
    private void loadVault() throws Exception {
        if (!Files.exists(vaultPath)) {
            // New vault, no entries yet
            this.entries = new ArrayList<>();
            return;
        }

        // Read encrypted data
        String encryptedData = Files.readString(vaultPath);

        // Decrypt
        String json = encryptionService.decrypt(encryptedData, masterPassword);

        // Parse JSON
        Type listType = new TypeToken<ArrayList<PasswordEntry>>() {}.getType();
        this.entries = gson.fromJson(json, listType);

        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
    }

    /**
     * Save vault to encrypted file
     */
    public void saveVault() throws Exception {
        initializeAppDirectory();

        // Convert entries to JSON
        String json = gson.toJson(entries);

        // Encrypt
        String encrypted = encryptionService.encrypt(json, masterPassword);

        // Save to file
        Files.writeString(vaultPath, encrypted);
    }

    /**
     * Get all password entries
     */
    public List<PasswordEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Add a new password entry
     */
    public void addEntry(PasswordEntry entry) throws Exception {
        if (entry.getWebsite() == null || entry.getWebsite().trim().isEmpty()) {
            throw new IllegalArgumentException("Website is required");
        }
        if (entry.getUsername() == null || entry.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (entry.getPassword() == null || entry.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        entries.add(entry);
        saveVault();
    }

    /**
     * Update an existing password entry
     */
    public void updateEntry(PasswordEntry oldEntry, PasswordEntry newEntry) throws Exception {
        int index = entries.indexOf(oldEntry);
        if (index != -1) {
            newEntry.setModifiedDate(LocalDateTime.now());
            entries.set(index, newEntry);
            saveVault();
        }
    }

    /**
     * Delete a password entry
     */
    public void deleteEntry(PasswordEntry entry) throws Exception {
        entries.remove(entry);
        saveVault();
    }

    /**
     * Search entries by keyword
     */
    public List<PasswordEntry> searchEntries(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getEntries();
        }

        String lowerKeyword = keyword.toLowerCase();
        return entries.stream()
                .filter(entry ->
                        (entry.getWebsite() != null && entry.getWebsite().toLowerCase().contains(lowerKeyword)) ||
                        (entry.getUsername() != null && entry.getUsername().toLowerCase().contains(lowerKeyword)) ||
                        (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }

    /**
     * Adapter for LocalDateTime JSON serialization
     */
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}
