package com.example.insecurebank.service;

import com.example.insecurebank.domain.RevokedToken;
import com.example.insecurebank.repository.RevokedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository != null ? revokedTokenRepository : null;
    }

    @Transactional
    public void revokeToken(String token, String tokenType, String reason) {
        if (isTokenRevoked(token)) {
            return;
        }

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);
        revokedToken.setTokenType(tokenType);
        revokedToken.setRevokedAt(Instant.now());
        revokedToken.setReason(reason);

        revokedTokenRepository.save(revokedToken);
    }

    public boolean isTokenRevoked(String token) {
        return revokedTokenRepository.findByToken(token).isPresent();
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldRevokedTokens() {
        Instant cutoffDate = Instant.now().minus(30, ChronoUnit.DAYS);
        revokedTokenRepository.deleteByRevokedAtBefore(cutoffDate);
    }
}
