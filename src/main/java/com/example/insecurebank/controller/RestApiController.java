package com.example.insecurebank.controller;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.User;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.InsecureUserDao;
import com.example.insecurebank.service.InsecureTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApiController {

    private final InsecureUserDao insecureUserDao;
    private final InsecureTokenService tokenService;
    private final BankAccountRepository bankAccountRepository;
    private final ObjectMapper objectMapper;

    public RestApiController(InsecureUserDao insecureUserDao,
                             InsecureTokenService tokenService,
                             BankAccountRepository bankAccountRepository,
                             ObjectMapper objectMapper) {
        this.insecureUserDao = insecureUserDao;
        this.tokenService = tokenService;
        this.bankAccountRepository = bankAccountRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("login");
        String password = body.get("password");

        User user = insecureUserDao.findByLoginAndPassword(username, password);
        
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
       
        String token = tokenService.generateToken(user.getId(), "USER");
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/api/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Long userId = extractUserId(authorization);
        BankAccount account = bankAccountRepository.findById(id).orElse(null);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "accountId", account.getId(),
                "accountNumber", account.getAccountNumber(),
                "balance", account.getBalance(),
                "requestedByUserId", userId
        ));
    }

    private Long extractUserId(String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode node = objectMapper.readTree(new String(payloadBytes, StandardCharsets.UTF_8));
            return node.has("userId") ? node.get("userId").asLong() : null;
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }
    }
}
