package com.passwordmanager.util;

public enum PasswordStrength {
    WEAK("Weak", "#f44336"),      // Red
    MEDIUM("Medium", "#ff9800"),  // Orange
    STRONG("Strong", "#4caf50");  // Green

    private final String label;
    private final String color;

    PasswordStrength(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    /**
     * Calculate password strength based on various criteria
     */
    public static PasswordStrength calculate(String password) {
        if (password == null || password.isEmpty()) {
            return WEAK;
        }

        int score = 0;
        int length = password.length();

        // Length scoring
        if (length >= 8) score++;
        if (length >= 12) score++;
        if (length >= 16) score++;

        // Character variety scoring
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++; // special chars

        // Determine strength
        if (score >= 6) {
            return STRONG;
        } else if (score >= 3) {
            return MEDIUM;
        } else {
            return WEAK;
        }
    }
}
