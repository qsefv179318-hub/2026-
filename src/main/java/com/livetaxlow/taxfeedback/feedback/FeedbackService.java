package com.livetaxlow.taxfeedback.feedback;

import com.livetaxlow.taxfeedback.payment.Payment;
import com.livetaxlow.taxfeedback.tax.TaxRule;
import com.livetaxlow.taxfeedback.tax.TaxRuleRepository;
import com.livetaxlow.taxfeedback.user.UserProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TaxRuleRepository taxRuleRepository;
    private final AiFeedbackGenerator aiFeedbackGenerator;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            TaxRuleRepository taxRuleRepository,
            AiFeedbackGenerator aiFeedbackGenerator
    ) {
        this.feedbackRepository = feedbackRepository;
        this.taxRuleRepository = taxRuleRepository;
        this.aiFeedbackGenerator = aiFeedbackGenerator;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void generateForPayment(UserProfile user, Payment payment) {
        int taxYear = LocalDate.now().getYear();
        String categoryCode = payment.getCategory().getCode();

        if (!payment.isDeductible()) {
            feedbackRepository.save(new Feedback(
                    user,
                    payment,
                    FeedbackType.WARNING,
                    "공제 제외 가능성이 높은 결제입니다",
                    payment.getMerchantName() + " 결제는 현재 공제 제외 후보로 분류되었습니다. 세부 업종 확인이 필요합니다.",
                    0
            ));
            return;
        }

        TaxRule rule = taxRuleRepository.findByTaxYearAndCategory_Code(taxYear, categoryCode).orElse(null);
        long expectedBenefit = rule == null ? 0 : BigDecimal.valueOf(payment.getAmount())
                .multiply(rule.getDeductionRate())
                .multiply(BigDecimal.valueOf(0.15))
                .setScale(0, RoundingMode.DOWN)
                .longValue();

        FeedbackType type = switch (categoryCode) {
            case "DEBIT_CARD", "CASH_RECEIPT", "PUBLIC_TRANSPORT", "TRADITIONAL_MARKET" -> FeedbackType.GOOD;
            case "MEDICAL" -> FeedbackType.WARNING;
            default -> FeedbackType.TIP;
        };

        String title = switch (categoryCode) {
            case "DEBIT_CARD", "CASH_RECEIPT" -> "공제율이 높은 결제수단 사용분입니다";
            case "PUBLIC_TRANSPORT" -> "대중교통 공제 후보 거래입니다";
            case "TRADITIONAL_MARKET" -> "전통시장 공제 후보 거래입니다";
            case "MEDICAL" -> "의료비 공제 기준 확인이 필요합니다";
            case "CULTURE" -> "문화비 공제 후보 거래입니다";
            default -> "소득공제 후보 거래입니다";
        };

        String message = payment.getMerchantName() + " " + payment.getAmount()
                + "원 결제가 " + payment.getCategory().getDisplayName()
                + " 항목으로 분류되었습니다.";

        AiFeedbackGenerator.AiFeedback aiFeedback = aiFeedbackGenerator.generate(payment, type, title, message, expectedBenefit);
        feedbackRepository.save(new Feedback(user, payment, aiFeedback.type(), aiFeedback.title(), aiFeedback.message(), expectedBenefit));
    }

    @Transactional(readOnly = true)
    public List<Feedback> list(UUID userId) {
        return feedbackRepository.findTop20ByUser_IdOrderByCreatedAtDesc(userId);
    }
}
