package com.Smart.Contact.Manager.utils;

import java.util.UUID;

public class TokenUtils {
    // Utility method to generate a unique reset token
    public static String generateResetToken() {
        return UUID.randomUUID().toString();  // Generate a unique UUID as the token
    }
}
