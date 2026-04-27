package org.example.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String passwordHash) {
        return BCrypt.checkpw(plainPassword, passwordHash);
    }
}