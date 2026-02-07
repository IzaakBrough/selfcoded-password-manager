package com.passwordmanager.service;

import com.passwordmanager.util.PasswordStrength;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorServiceTest {

    @Test
    void testGeneratePasswordWithAllOptions() {
        PasswordGeneratorService service = new PasswordGeneratorService();
        String password = service.generatePassword(16, true, true, true, true);

        assertNotNull(password);
        assertEquals(16, password.length());
    }

    @Test
    void testGeneratePasswordLength() {
        PasswordGeneratorService service = new PasswordGeneratorService();

        for (int length = 8; length <= 32; length += 4) {
            String password = service.generatePassword(length, true, true, true, true);
            assertEquals(length, password.length());
        }
    }

    @Test
    void testGeneratePasswordInvalidLength() {
        PasswordGeneratorService service = new PasswordGeneratorService();

        assertThrows(IllegalArgumentException.class, () -> {
            service.generatePassword(5, true, true, true, true);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.generatePassword(50, true, true, true, true);
        });
    }

    @Test
    void testGenerateStrongPassword() {
        PasswordGeneratorService service = new PasswordGeneratorService();
        String password = service.generateStrongPassword(20);

        assertNotNull(password);
        assertEquals(20, password.length());

        // A strong password should likely be STRONG or MEDIUM
        PasswordStrength strength = service.calculateStrength(password);
        assertTrue(strength == PasswordStrength.STRONG || strength == PasswordStrength.MEDIUM,
                "Generated strong password should be at least MEDIUM strength");
    }

    @Test
    void testPasswordStrengthCalculation() {
        PasswordGeneratorService service = new PasswordGeneratorService();

        // Weak password
        PasswordStrength weakStrength = service.calculateStrength("abc");
        assertEquals(PasswordStrength.WEAK, weakStrength);

        // Medium password
        PasswordStrength mediumStrength = service.calculateStrength("Password123");
        assertTrue(mediumStrength == PasswordStrength.MEDIUM || mediumStrength == PasswordStrength.STRONG);

        // Strong password
        PasswordStrength strongStrength = service.calculateStrength("MyP@ssw0rd!2024");
        assertEquals(PasswordStrength.STRONG, strongStrength);
    }

    @Test
    void testGeneratePasswordUniqueness() {
        PasswordGeneratorService service = new PasswordGeneratorService();

        // Generate multiple passwords and ensure they're different
        String password1 = service.generateStrongPassword(16);
        String password2 = service.generateStrongPassword(16);
        String password3 = service.generateStrongPassword(16);

        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }
}
