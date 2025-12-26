package com.example.insecurebank.controller;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.User;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.UserRepository;
import com.example.insecurebank.service.JwtTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApiController {

    private final JwtTokenService tokenService;
    private final BankAccountRepository bankAccountRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public RestApiController(JwtTokenService tokenService,
                             BankAccountRepository bankAccountRepository,
                             AuthenticationManager authenticationManager,
                             UserRepository userRepository) {
        this.tokenService = tokenService;
        this.bankAccountRepository = bankAccountRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("login");
        String password = body.get("password");

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = tokenService.generateToken(user.getId(), "USER");
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/api/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id,
                                        org.springframework.security.core.Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
             return ResponseEntity.status(401).build();
        }

        Long userId;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwtPrincipal) {
            Object userIdClaim = jwtPrincipal.getClaim("userId");
            if (userIdClaim != null) {
                userId = Long.valueOf(userIdClaim.toString());
            } else {
                try {
                    userId = Long.parseLong(jwtPrincipal.getSubject());
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(401).build();
                }
            }
        } else {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(401).build();
            }
        }

        BankAccount account = bankAccountRepository.findById(id).orElse(null);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        if (!account.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(Map.of(
                "accountId", account.getId(),
                "accountNumber", account.getAccountNumber(),
                "balance", account.getBalance(),
                "requestedByUserId", userId
        ));
    }
}
