package com.livetaxlow.taxfeedback.feedback;

import com.fasterxml.jackson.databind.JsonNode;
import com.livetaxlow.taxfeedback.payment.Payment;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiFeedbackGenerator implements AiFeedbackGenerator {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public OpenAiFeedbackGenerator(
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            WebClient.Builder builder
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
    }

    @Override
    public AiFeedback generate(Payment payment, FeedbackType fallbackType, String fallbackTitle, String fallbackMessage, long expectedBenefit) {
        if (apiKey.isBlank()) {
            return new AiFeedback(fallbackType, fallbackTitle, fallbackMessage);
        }

        try {
            JsonNode response = webClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(requestBody(payment, fallbackType, fallbackTitle, fallbackMessage, expectedBenefit))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            String text = extractOutputText(response);
            if (text == null || text.isBlank()) {
                return new AiFeedback(fallbackType, fallbackTitle, fallbackMessage);
            }
            return new AiFeedback(fallbackType, fallbackTitle, text.trim());
        } catch (Exception ignored) {
            return new AiFeedback(fallbackType, fallbackTitle, fallbackMessage);
        }
    }

    private Map<String, Object> requestBody(Payment payment, FeedbackType fallbackType, String fallbackTitle, String fallbackMessage, long expectedBenefit) {
        String prompt = """
                당신은 한국 연말정산 절세 피드백 어시스턴트입니다.
                사용자가 방금 결제한 거래를 보고, 앱 알림에 들어갈 짧은 한국어 피드백을 작성하세요.
                과장하지 말고, 세무 확정 표현 대신 '예상', '후보', '확인 필요'처럼 신중하게 말하세요.
                2문장 이내로 작성하세요.

                거래 정보:
                - 가맹점: %s
                - 금액: %d원
                - 결제수단: %s
                - 공제 카테고리: %s
                - 기본 분류: %s
                - 기본 제목: %s
                - 기본 메시지: %s
                - 예상 절세 효과: %d원
                """.formatted(
                payment.getMerchantName(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getCategory().getDisplayName(),
                fallbackType,
                fallbackTitle,
                fallbackMessage,
                expectedBenefit
        );

        return Map.of(
                "model", model,
                "input", List.of(Map.of("role", "user", "content", prompt)),
                "max_output_tokens", 220
        );
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            return null;
        }
        if (response.hasNonNull("output_text")) {
            return response.get("output_text").asText();
        }
        JsonNode output = response.get("output");
        if (output == null || !output.isArray()) {
            return null;
        }
        for (JsonNode item : output) {
            JsonNode content = item.get("content");
            if (content == null || !content.isArray()) {
                continue;
            }
            for (JsonNode contentItem : content) {
                if (contentItem.hasNonNull("text")) {
                    return contentItem.get("text").asText();
                }
            }
        }
        return null;
    }
}
