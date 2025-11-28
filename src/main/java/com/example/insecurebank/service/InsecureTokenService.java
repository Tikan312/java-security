package com.example.insecurebank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class InsecureTokenService {

    // INSECURE: weak, hardcoded signing key easily guessable/bruteforced
    private static final String SECRET = "secret";

    private final ObjectMapper objectMapper;

    public InsecureTokenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateToken(Long userId, String role) {
        try {
            // INSECURE: no header/alg validation; assumes HMAC without allowing for algorithm confusion
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

            TokenPayload payloadObj = new TokenPayload(userId, role);
            String payloadJson = objectMapper.writeValueAsString(payloadObj);
            // INSECURE: no exp/iat/aud claims; tokens never expire and are not scoped
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String unsignedToken = header + "." + payload;
            String signature = sign(unsignedToken);
            return unsignedToken + "." + signature;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create token", e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    private record TokenPayload(Long userId, String role) {
    }
}
