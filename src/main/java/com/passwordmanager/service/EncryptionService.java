package com.passwordmanager.service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionService {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    private final SecureRandom random;

    public EncryptionService() {
        this.random = new SecureRandom();
    }

    /**
     * Derive encryption key from master password using PBKDF2
     */
    private SecretKey deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), KEY_ALGORITHM);
    }

    /**
     * Encrypt data with AES-256-CBC
     *
     * @param data           Data to encrypt
     * @param masterPassword Master password for key derivation
     * @return Base64 encoded string: salt:iv:ciphertext
     */
    public String encrypt(String data, String masterPassword) throws Exception {
        try {
            // Generate random salt
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Derive key from password
            SecretKey key = deriveKey(masterPassword, salt);

            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Encode to Base64: salt:iv:ciphertext
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String encryptedB64 = Base64.getEncoder().encodeToString(encrypted);

            return saltB64 + ":" + ivB64 + ":" + encryptedB64;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeySpecException e) {
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt data with AES-256-CBC
     *
     * @param encryptedData  Base64 encoded encrypted data (salt:iv:ciphertext)
     * @param masterPassword Master password for key derivation
     * @return Decrypted plaintext
     */
    public String decrypt(String encryptedData, String masterPassword) throws Exception {
        try {
            // Split the encrypted data
            String[] parts = encryptedData.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);

            // Derive key from password
            SecretKey key = deriveKey(masterPassword, salt);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeySpecException e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }
}
