package com.example.insecurebank.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.springframework.stereotype.Service;

@Service
public class SecureCryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final KeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecureCryptoService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        if (plaintextBytes.length == 0) {
            throw new IllegalArgumentException("plaintext must not be empty");
        }

        try {
            SecretKey key = keyProvider.getEncryptionKey();
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plaintextBytes);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return java.util.Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt data", e);
        }
    }

    public String decrypt(String base64CipherText) {
        if (base64CipherText == null) {
            throw new IllegalArgumentException("cipher text must not be null");
        }
        if (base64CipherText.isBlank()) {
            throw new IllegalArgumentException("cipher text must not be blank");
        }

        try {
            byte[] allBytes = java.util.Base64.getDecoder().decode(base64CipherText);
            if (allBytes.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("cipher text is too short");
            }

            ByteBuffer buffer = ByteBuffer.wrap(allBytes);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            SecretKey key = keyProvider.getEncryptionKey();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintextBytes = cipher.doFinal(cipherText);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt data", e);
        }
    }
}
