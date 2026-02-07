package com.passwordmanager.service;

import com.passwordmanager.util.PasswordStrength;

import java.security.SecureRandom;

public class PasswordGeneratorService {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom random;

    public PasswordGeneratorService() {
        this.random = new SecureRandom();
    }

    /**
     * Generate a password with specified options
     *
     * @param length         Password length (8-32)
     * @param useUppercase   Include uppercase letters
     * @param useLowercase   Include lowercase letters
     * @param useNumbers     Include numbers
     * @param useSymbols     Include symbols
     * @return Generated password
     */
    public String generatePassword(int length, boolean useUppercase, boolean useLowercase,
                                    boolean useNumbers, boolean useSymbols) {
        // Validate length
        if (length < 8 || length > 32) {
            throw new IllegalArgumentException("Password length must be between 8 and 32 characters");
        }

        // Build character set
        StringBuilder charset = new StringBuilder();
        if (useUppercase) charset.append(UPPERCASE);
        if (useLowercase) charset.append(LOWERCASE);
        if (useNumbers) charset.append(NUMBERS);
        if (useSymbols) charset.append(SYMBOLS);

        // Ensure at least one character type is selected
        if (charset.length() == 0) {
            charset.append(LOWERCASE); // Default to lowercase
        }

        // Generate password
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charset.length());
            password.append(charset.charAt(index));
        }

        return password.toString();
    }

    /**
     * Generate a strong password with all character types
     *
     * @param length Password length
     * @return Generated strong password
     */
    public String generateStrongPassword(int length) {
        return generatePassword(length, true, true, true, true);
    }

    /**
     * Calculate the strength of a password
     *
     * @param password Password to evaluate
     * @return PasswordStrength enum
     */
    public PasswordStrength calculateStrength(String password) {
        return PasswordStrength.calculate(password);
    }
}
