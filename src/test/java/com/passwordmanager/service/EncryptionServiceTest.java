package com.passwordmanager.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    @Test
    void testEncryptionDecryption() throws Exception {
        EncryptionService service = new EncryptionService();
        String originalData = "This is a test password entry";
        String masterPassword = "MySecretMasterPass123!";

        // Encrypt
        String encrypted = service.encrypt(originalData, masterPassword);
        assertNotNull(encrypted);
        assertNotEquals(originalData, encrypted);
        assertTrue(encrypted.contains(":"), "Encrypted data should contain colons");

        // Decrypt
        String decrypted = service.decrypt(encrypted, masterPassword);
        assertEquals(originalData, decrypted);
    }

    @Test
    void testDecryptionWithWrongPassword() {
        EncryptionService service = new EncryptionService();
        String originalData = "Secret data";
        String correctPassword = "CorrectPassword123!";
        String wrongPassword = "WrongPassword456!";

        try {
            String encrypted = service.encrypt(originalData, correctPassword);
            assertThrows(Exception.class, () -> {
                service.decrypt(encrypted, wrongPassword);
            });
        } catch (Exception e) {
            fail("Encryption should not fail: " + e.getMessage());
        }
    }

    @Test
    void testMultipleEncryptions() throws Exception {
        EncryptionService service = new EncryptionService();
        String data = "Test data";
        String password = "Password123!";

        // Encrypt the same data twice
        String encrypted1 = service.encrypt(data, password);
        String encrypted2 = service.encrypt(data, password);

        // They should be different (due to random IV and salt)
        assertNotEquals(encrypted1, encrypted2);

        // But both should decrypt to the same data
        assertEquals(data, service.decrypt(encrypted1, password));
        assertEquals(data, service.decrypt(encrypted2, password));
    }
}
