package com.example.insecurebank.service;

import com.example.insecurebank.domain.User;
import com.example.insecurebank.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OidcUserInfoService {

    private final UserRepository userRepository;

    public OidcUserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public OidcUserInfo loadUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("name", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("email_verified", false);

        return OidcUserInfo.builder()
                .claims(c -> c.putAll(claims))
                .build();
    }
}
