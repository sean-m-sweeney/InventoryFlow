package com.inventoryflow.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Manages SQLite database operations for storing encrypted credentials.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:inventoryflow.db";
    private static final String ENCRYPTION_KEY_ENV = "INVENTORYFLOW_SECRET";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void createTables() throws SQLException {
        String createSettingsTable = """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSettingsTable);
        }
    }

    /**
     * Stores a hashed PIN in the database.
     */
    public void storePin(String pin) {
        String hashedPin = hashPin(pin);
        storeSetting("pin_hash", hashedPin);
    }

    /**
     * Validates the provided PIN against the stored hash.
     */
    public boolean validatePin(String pin) {
        String storedHash = getSetting("pin_hash");
        if (storedHash == null) {
            return false;
        }
        return storedHash.equals(hashPin(pin));
    }

    /**
     * Checks if a PIN has been configured.
     */
    public boolean isPinConfigured() {
        return getSetting("pin_hash") != null;
    }

    /**
     * Stores the encrypted Shopify API token.
     */
    public void storeShopifyToken(String token) {
        String encrypted = encrypt(token);
        storeSetting("shopify_token", encrypted);
    }

    /**
     * Retrieves and decrypts the Shopify API token.
     */
    public String getShopifyToken() {
        String encrypted = getSetting("shopify_token");
        if (encrypted == null) {
            return null;
        }
        return decrypt(encrypted);
    }

    /**
     * Stores the Shopify store domain.
     */
    public void storeShopifyDomain(String domain) {
        storeSetting("shopify_domain", domain);
    }

    /**
     * Retrieves the Shopify store domain.
     */
    public String getShopifyDomain() {
        return getSetting("shopify_domain");
    }

    /**
     * Checks if Shopify credentials are configured.
     */
    public boolean isShopifyConfigured() {
        return getShopifyToken() != null && getShopifyDomain() != null;
    }

    private void storeSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to store setting: " + key, e);
        }
    }

    private String getSetting(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve setting: " + key, e);
        }
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private byte[] getEncryptionKey() {
        String envKey = System.getenv(ENCRYPTION_KEY_ENV);
        if (envKey == null || envKey.isEmpty()) {
            // Use a default key derived from a fixed value (for development)
            // In production, INVENTORYFLOW_SECRET environment variable should be set
            envKey = "InventoryFlowDefaultKey2024";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(envKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String encrypt(String plaintext) {
        try {
            byte[] key = getEncryptionKey();
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private String decrypt(String encryptedBase64) {
        try {
            byte[] key = getEncryptionKey();
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // Extract IV and ciphertext
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Ignore close errors
        }
    }
}
