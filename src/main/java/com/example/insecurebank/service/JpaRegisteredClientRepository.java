package com.example.insecurebank.service;

import com.example.insecurebank.domain.OAuth2RegisteredClientEntity;
import com.example.insecurebank.repository.OAuth2ClientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    public JpaRegisteredClientRepository(OAuth2ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        OAuth2RegisteredClientEntity entity = toEntity(registeredClient);
        clientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private OAuth2RegisteredClientEntity toEntity(RegisteredClient client) {
        OAuth2RegisteredClientEntity entity = new OAuth2RegisteredClientEntity();
        entity.setId(client.getId());
        entity.setClientId(client.getClientId());
        entity.setClientSecret(client.getClientSecret());
        entity.setClientIdIssuedAt(client.getClientIdIssuedAt() != null ? client.getClientIdIssuedAt() : Instant.now());
        entity.setClientSecretExpiresAt(client.getClientSecretExpiresAt());
        entity.setClientName(client.getClientName());

        entity.setClientAuthenticationMethods(writeSet(client.getClientAuthenticationMethods()));
        entity.setAuthorizationGrantTypes(writeSet(client.getAuthorizationGrantTypes()));
        entity.setRedirectUris(writeSet(client.getRedirectUris()));
        entity.setPostLogoutRedirectUris(writeSet(client.getPostLogoutRedirectUris()));
        entity.setScopes(writeSet(client.getScopes()));

        entity.setClientSettings(writeMap(client.getClientSettings().getSettings()));
        entity.setTokenSettings(writeMap(client.getTokenSettings().getSettings()));

        return entity;
    }

    private RegisteredClient toRegisteredClient(OAuth2RegisteredClientEntity entity) {
        Set<String> clientAuthenticationMethods = readSet(entity.getClientAuthenticationMethods());
        Set<String> authorizationGrantTypes = readSet(entity.getAuthorizationGrantTypes());
        Set<String> redirectUris = readSet(entity.getRedirectUris());
        Set<String> postLogoutRedirectUris = readSet(entity.getPostLogoutRedirectUris());
        Set<String> scopes = readSet(entity.getScopes());

        RegisteredClient.Builder builder = RegisteredClient.withId(entity.getId())
                .clientId(entity.getClientId())
                .clientIdIssuedAt(entity.getClientIdIssuedAt())
                .clientSecret(entity.getClientSecret())
                .clientSecretExpiresAt(entity.getClientSecretExpiresAt())
                .clientName(entity.getClientName());

        clientAuthenticationMethods.forEach(method ->
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
        authorizationGrantTypes.forEach(grantType ->
                builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
        redirectUris.forEach(builder::redirectUri);
        postLogoutRedirectUris.forEach(builder::postLogoutRedirectUri);
        scopes.forEach(builder::scope);

        Map<String, Object> clientSettingsMap = readMap(entity.getClientSettings());
        builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

        Map<String, Object> tokenSettingsMap = readMap(entity.getTokenSettings());
        builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());

        return builder.build();
    }

    private String writeSet(Set<?> set) {
        try {
            return objectMapper.writeValueAsString(set);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize set", e);
        }
    }

    private static class StringTypeReference extends TypeReference<Set<String>> {}
    private static class MapTypeReference extends TypeReference<Set<Map<String, Object>>> {}
    private static class SettingsMapTypeReference extends TypeReference<Map<String, Object>> {}

    private Set<String> readSet(String json) {
        try {
            return objectMapper.readValue(json, new StringTypeReference());
        } catch (java.io.IOException primary) {
            try {
                Set<Map<String, Object>> legacy = objectMapper.readValue(json, new MapTypeReference());
                return legacy.stream()
                        .map(entry -> entry.get("value"))
                        .filter(v -> v != null)
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet());
            } catch (java.io.IOException secondary) {
                throw new RuntimeException("Failed to deserialize set", secondary);
            }
        }
    }

    private String writeMap(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize map", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        try {
            return objectMapper.readValue(json, new SettingsMapTypeReference());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize map", e);
        }
    }
}
