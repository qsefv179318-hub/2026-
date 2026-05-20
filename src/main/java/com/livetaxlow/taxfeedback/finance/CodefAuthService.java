package com.livetaxlow.taxfeedback.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// Issues CODEF OAuth access tokens using client_credentials grant.
@Service
public class CodefAuthService {

    private static final Logger log = LoggerFactory.getLogger(CodefAuthService.class);
    private static final String CODEF_OAUTH_URL = "https://oauth.codef.io";

    private final CodefProperties codefProperties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public CodefAuthService(
            CodefProperties codefProperties,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder
    ) {
        this.codefProperties = codefProperties;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(CODEF_OAUTH_URL).build();
    }

    public String issueAccessToken() {
        String clientId = codefProperties.clientId();
        String clientSecret = codefProperties.clientSecret();

        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.error("CODEF auth failed: missing client credentials (codef.client-id/codef.client-secret).");
            throw new IllegalStateException("Missing CODEF client credentials.");
        }

        try {
            String basicAuth = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)
            );

            String rawResponse = webClient.post()
                    .uri("/oauth/token")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("grant_type=client_credentials")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(rawResponse);
            String accessToken = jsonNode.path("access_token").asText(null);
            if (accessToken == null || accessToken.isBlank()) {
                log.error("CODEF auth failed: access_token is missing in response. response={}", rawResponse);
                throw new IllegalStateException("CODEF token response missing access_token.");
            }

            log.info("CODEF access token issued successfully.");
            return accessToken;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException exception) {
            log.error(
                    "CODEF auth HTTP error: status={}, responseBody={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );
            throw new IllegalStateException("CODEF auth request failed (HTTP).", exception);
        } catch (Exception exception) {
            log.error("CODEF auth unexpected error.", exception);
            throw new IllegalStateException("CODEF auth request failed.", exception);
        }
    }
}

