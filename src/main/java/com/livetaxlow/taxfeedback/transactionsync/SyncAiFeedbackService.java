package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.databind.JsonNode;
import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.RuleBasedDeductionResult;
import com.livetaxlow.taxfeedback.feedback.Feedback;
import com.livetaxlow.taxfeedback.feedback.FeedbackRepository;
import com.livetaxlow.taxfeedback.feedback.FeedbackType;
import com.livetaxlow.taxfeedback.finance.UserTransaction;
import com.livetaxlow.taxfeedback.finance.UserTransactionRepository;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.SyncResponse;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SyncAiFeedbackService {

    private final UserService userService;
    private final UserTransactionRepository userTransactionRepository;
    private final FeedbackRepository feedbackRepository;
    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public SyncAiFeedbackService(
            UserService userService,
            UserTransactionRepository userTransactionRepository,
            FeedbackRepository feedbackRepository,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            WebClient.Builder builder
    ) {
        this.userService = userService;
        this.userTransactionRepository = userTransactionRepository;
        this.feedbackRepository = feedbackRepository;
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
    }

    @Transactional
    public Feedback generateAndSave(UUID userId, SyncResponse sync, RuleBasedDeductionResult deduction) {
        UserProfile user = userService.get(userId);
        List<UserTransaction> recent = userTransactionRepository.findTop5ByUserIdOrderByApprovedAtDesc(userId);

        FeedbackType type = deduction.cardThresholdRemaining() > 0 ? FeedbackType.TIP : FeedbackType.GOOD;
        if (deduction.medicalThresholdAmount() > 0
                && deduction.medicalSpending() < deduction.medicalThresholdAmount() / 3) {
            type = FeedbackType.WARNING;
        }

        String title = "실시간 소비 피드백";
        String fallbackMessage = buildFallbackMessage(sync, deduction, recent);
        String message = generateAiMessage(deduction, recent, fallbackMessage);

        Feedback feedback = new Feedback(
                user,
                null,
                type,
                title,
                message,
                deduction.estimatedTaxReduction()
        );
        return feedbackRepository.save(feedback);
    }

    private String buildFallbackMessage(SyncResponse sync, RuleBasedDeductionResult deduction, List<UserTransaction> recent) {
        String latestMerchant = recent.isEmpty() ? "최근 거래 없음" : recent.get(0).getMerchantName();
        return """
                최근 동기화 %d건(중복 %d건) 반영됐습니다. 카드 공제 문턱까지 %,d원 남았고, 최신 소비는 %s 입니다.
                """.formatted(
                sync.importedCount(),
                sync.duplicateSkippedCount(),
                deduction.cardThresholdRemaining(),
                latestMerchant
        ).trim();
    }

    private String generateAiMessage(RuleBasedDeductionResult deduction, List<UserTransaction> recent, String fallbackMessage) {
        if (apiKey.isBlank()) {
            return fallbackMessage;
        }
        String recentText = recent.stream()
                .map(tx -> tx.getMerchantName() + " " + tx.getAmount() + "원(" + tx.getPaymentMethod() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("최근 거래 없음");
        String prompt = """
                당신은 한국 연말정산 절세 코치입니다.
                아래 실시간 카드 소비를 보고 사용자에게 2문장 이내로 피드백을 작성하세요.
                확정 표현 대신 '가능성', '권장', '확인'처럼 신중한 표현을 사용하세요.
                문턱값과 최근 결제 흐름을 반영하세요.
                최근 결제: %s
                카드 공제 문턱 잔여: %d원
                의료비 문턱: %d원, 현재 의료비: %d원
                예상 절세 효과: %d원
                """.formatted(
                recentText,
                deduction.cardThresholdRemaining(),
                deduction.medicalThresholdAmount(),
                deduction.medicalSpending(),
                deduction.estimatedTaxReduction()
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(Map.of(
                            "model", model,
                            "input", List.of(Map.of("role", "user", "content", prompt)),
                            "max_output_tokens", 200
                    ))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            String text = extractOutputText(response);
            if (text == null || text.isBlank()) {
                return fallbackMessage;
            }
            return text.trim();
        } catch (Exception exception) {
            return fallbackMessage;
        }
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

