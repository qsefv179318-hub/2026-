package com.livetaxlow.taxfeedback.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetaxlow.taxfeedback.payment.TossDtos.TossConfirmRequest;
import com.livetaxlow.taxfeedback.payment.TossDtos.TossErrorResponse;
import com.livetaxlow.taxfeedback.payment.TossDtos.TossPaymentResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TossPaymentsClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public TossPaymentsClient(
            @Value("${toss.payments.base-url}") String baseUrl,
            @Value("${toss.payments.secret-key}") String secretKey,
            WebClient.Builder builder,
            ObjectMapper objectMapper
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.secretKey = secretKey == null ? "" : secretKey.trim();
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, long amount) {
        ensureSecretKey();
        return webClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .bodyValue(new TossConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(toException(body))))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    public TossPaymentResponse getPayment(String paymentKey) {
        ensureSecretKey();
        return webClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(toException(body))))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    private String authorizationHeader() {
        String token = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private void ensureSecretKey() {
        if (secretKey.isBlank()) {
            throw new IllegalStateException("TOSS_SECRET_KEY is required to call Toss Payments API.");
        }
    }

    private TossPaymentsException toException(String body) {
        try {
            TossErrorResponse error = objectMapper.readValue(body, TossErrorResponse.class);
            return new TossPaymentsException(error.code(), error.message());
        } catch (Exception ignored) {
            return new TossPaymentsException("TOSS_API_ERROR", body);
        }
    }
}
