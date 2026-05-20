package com.livetaxlow.taxfeedback.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CodefClient {

    private final CodefProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiresAt;

    public CodefClient(CodefProperties properties, WebClient.Builder builder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchCardTransactions(ExternalFinanceLink link, Map<String, Object> query) {
        return fetchCardTransactions(
                link.getOrganization(),
                link.getConnectedId(),
                link.getClientType(),
                link.getAccountRef(),
                query
        );
    }

    public JsonNode createConnectedId(
            String organization,
            String clientType,
            String accountRef,
            Map<String, Object> credentialPayload
    ) {
        if (!properties.enabled()) {
            throw new IllegalStateException("CODEF integration is disabled. Set CODEF_ENABLED=true.");
        }

        String token = getAccessToken();
        Map<String, Object> payload = new HashMap<>();
        payload.put("organization", organization);
        payload.put("clientType", clientType == null || clientType.isBlank() ? "PERSONAL" : clientType);
        if (accountRef != null && !accountRef.isBlank()) {
            payload.put("accountRef", accountRef);
        }
        if (credentialPayload != null) {
            payload.putAll(credentialPayload);
        }

        String response = webClient.post()
                .uri(properties.accountCreatePath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readTree(response);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse CODEF connectedId response.", exception);
        }
    }

    public JsonNode fetchCardTransactions(
            String organization,
            String connectedId,
            String clientType,
            String accountRef,
            Map<String, Object> query
    ) {
        if (!properties.enabled()) {
            throw new IllegalStateException("CODEF integration is disabled. Set CODEF_ENABLED=true.");
        }

        String token = getAccessToken();
        Map<String, Object> payload = new HashMap<>(query);
        payload.put("organization", organization);
        payload.put("connectedId", connectedId);
        payload.put("clientType", clientType == null || clientType.isBlank() ? "PERSONAL" : clientType);
        if (accountRef != null && !accountRef.isBlank()) {
            payload.put("accountRef", accountRef);
        }

        String response = webClient.post()
                .uri(properties.transactionPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readTree(response);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse CODEF response.", exception);
        }
    }

    private String getAccessToken() {
        Instant now = Instant.now();
        if (cachedAccessToken != null && tokenExpiresAt != null && now.isBefore(tokenExpiresAt.minusSeconds(30))) {
            return cachedAccessToken;
        }

        ensureClientCredential();
        String auth = Base64.getEncoder().encodeToString(
                (properties.clientId() + ":" + properties.clientSecret()).getBytes(StandardCharsets.UTF_8)
        );

        String response = webClient.post()
                .uri(properties.tokenPath())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&scope=read")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode node = objectMapper.readTree(response);
            String token = text(node, "access_token");
            long expiresIn = longValue(node, "expires_in", 300L);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("CODEF token response does not contain access_token.");
            }
            cachedAccessToken = token;
            tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
            return token;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse CODEF token response.", exception);
        }
    }

    private void ensureClientCredential() {
        if (properties.clientId() == null || properties.clientId().isBlank()
                || properties.clientSecret() == null || properties.clientSecret().isBlank()) {
            throw new IllegalStateException("CODEF_CLIENT_ID and CODEF_CLIENT_SECRET are required.");
        }
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private long longValue(JsonNode node, String field, long fallback) {
        return node.hasNonNull(field) ? node.get(field).asLong() : fallback;
    }
}
