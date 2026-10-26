package com.foodordering.repository;

import com.foodordering.config.DatabaseConnection;
import com.foodordering.model.Customer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/** Stores customer accounts with salted PBKDF2 password hashes. */
public class AuthRepository {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthRepository() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS customer_accounts ("
                             + "account_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                             + "name VARCHAR(120) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE, "
                             + "password_salt VARCHAR(64) NOT NULL, password_hash VARCHAR(64) NOT NULL, "
                             + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)")) {
            statement.execute();
        }
    }

    public void register(String name, String email, char[] password) throws SQLException {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String sql = "INSERT INTO customer_accounts (name, email, password_salt, password_hash) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, Base64.getEncoder().encodeToString(salt));
            statement.setString(4, hash(password, salt));
            statement.executeUpdate();
        }
    }

    public Customer login(String email, char[] password) throws SQLException {
        String sql = "SELECT name, password_salt, password_hash FROM customer_accounts WHERE email = ?";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                byte[] salt = Base64.getDecoder().decode(result.getString("password_salt"));
                byte[] expected = Base64.getDecoder().decode(result.getString("password_hash"));
                byte[] actual = Base64.getDecoder().decode(hash(password, salt));
                if (!MessageDigest.isEqual(expected, actual)) return null;
                return new Customer(result.getString("name"), email, "", "");
            }
        }
    }

    private static String hash(char[] password, byte[] salt) throws SQLException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new SQLException("Could not securely process the password.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
