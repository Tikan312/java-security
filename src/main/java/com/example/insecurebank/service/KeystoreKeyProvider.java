package com.example.insecurebank.service;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class KeystoreKeyProvider implements KeyProvider {

    private final SecretKey secretKey;

    public KeystoreKeyProvider(
            @Value("${app.crypto.keystore.location}") Resource keystoreLocation,
            @Value("${app.crypto.keystore.password}") String keystorePassword,
            @Value("${app.crypto.keystore.alias}") String keyAlias) {
        this.secretKey = initializeKey(keystoreLocation, keystorePassword, keyAlias);
    }

    private SecretKey initializeKey(Resource keystoreLocation, String keystorePassword, String keyAlias) {
        if (keystoreLocation == null || keystorePassword == null || keyAlias == null) {
            throw new IllegalStateException("Keystore configuration properties must be set");
        }

        try (InputStream is = keystoreLocation.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, keystorePassword.toCharArray());

            if (!keyStore.isKeyEntry(keyAlias)) {
                throw new IllegalStateException("Key alias '" + keyAlias + "' not found in keystore");
            }

            Key key = keyStore.getKey(keyAlias, keystorePassword.toCharArray());
            if (!(key instanceof SecretKey)) {
                throw new IllegalStateException("Key with alias '" + keyAlias + "' is not a SecretKey");
            }

            return (SecretKey) key;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException
                 | CertificateException | java.io.IOException e) {
            throw new IllegalStateException("Failed to load encryption key from keystore", e);
        }
    }

    @Override
    public SecretKey getEncryptionKey() {
        if (secretKey == null) {
            throw new IllegalStateException("Encryption key is not initialized");
        }
        return secretKey;
    }
}
