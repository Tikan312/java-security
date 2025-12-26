package com.example.insecurebank.controller;

import com.example.insecurebank.service.TokenRevocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class TokenRevocationController {

    private final TokenRevocationService tokenRevocationService;

    public TokenRevocationController(TokenRevocationService tokenRevocationService) {
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeToken(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized_client"));
        }

        String tokenType = tokenTypeHint != null ? tokenTypeHint : "access_token";
        tokenRevocationService.revokeToken(token, tokenType, "Revoked by client");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/status")
    public ResponseEntity<?> checkTokenStatus(@RequestParam("token") String token) {
        boolean isRevoked = tokenRevocationService.isTokenRevoked(token);
        return ResponseEntity.ok(Map.of(
                "token", token.substring(0, Math.min(20, token.length())) + "...",
                "revoked", isRevoked
        ));
    }
}
