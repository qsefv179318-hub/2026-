package com.livetaxlow.taxfeedback.transactionsync;

import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.RuleBasedDeductionResult;
import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionEngineService;
import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.feedback.Feedback;
import com.livetaxlow.taxfeedback.finance.ExpenseCategory;
import com.livetaxlow.taxfeedback.finance.ExpenseCategoryRepository;
import com.livetaxlow.taxfeedback.finance.ExternalFinanceLink;
import com.livetaxlow.taxfeedback.finance.ExternalFinanceLinkRepository;
import com.livetaxlow.taxfeedback.finance.UserTransaction;
import com.livetaxlow.taxfeedback.finance.UserTransactionRepository;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.AutoSyncResponse;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.SyncFeedback;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.SyncResponse;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.ThresholdProgress;
import com.livetaxlow.taxfeedback.transactionsync.TransactionSource.FetchedTransaction;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Orchestrates transaction sync: CODEF call, parsing, category mapping, and idempotent DB insert.
@Service
public class SyncService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final TransactionSource transactionSource;
    private final SyncSourceProperties syncSourceProperties;
    private final UserService userService;
    private final ExternalFinanceLinkRepository externalFinanceLinkRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final CategoryMapper categoryMapper;
    private final RuleBasedDeductionEngineService deductionEngineService;
    private final SyncAiFeedbackService syncAiFeedbackService;

    public SyncService(
            TransactionSource transactionSource,
            SyncSourceProperties syncSourceProperties,
            UserService userService,
            ExternalFinanceLinkRepository externalFinanceLinkRepository,
            UserTransactionRepository userTransactionRepository,
            ExpenseCategoryRepository expenseCategoryRepository,
            CategoryMapper categoryMapper,
            RuleBasedDeductionEngineService deductionEngineService,
            SyncAiFeedbackService syncAiFeedbackService
    ) {
        this.transactionSource = transactionSource;
        this.syncSourceProperties = syncSourceProperties;
        this.userService = userService;
        this.externalFinanceLinkRepository = externalFinanceLinkRepository;
        this.userTransactionRepository = userTransactionRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.categoryMapper = categoryMapper;
        this.deductionEngineService = deductionEngineService;
        this.syncAiFeedbackService = syncAiFeedbackService;
    }

    @Transactional
    public AutoSyncResponse autoSyncAndCalculate(UUID userId) {
        ExternalFinanceLink link = externalFinanceLinkRepository
                .findByUserIdAndProviderAndActiveTrue(userId, "CODEF")
                .orElseThrow(() -> new NotFoundException("Active CODEF link not found for user: " + userId));

        SyncResponse sync = syncLast30Days(userId, link.getConnectedId(), link.getOrganization());
        int taxYear = LocalDate.now(KOREA_ZONE).getYear();
        RuleBasedDeductionResult deduction = deductionEngineService.preview(userId, taxYear);

        long cardUsed = Math.max(0, deduction.cardThresholdAmount() - deduction.cardThresholdRemaining());
        ThresholdProgress cardProgress = toProgress(deduction.cardThresholdAmount(), cardUsed, deduction.cardThresholdRemaining());

        long medicalUsed = Math.min(deduction.medicalSpending(), deduction.medicalThresholdAmount());
        long medicalRemaining = Math.max(0, deduction.medicalThresholdAmount() - medicalUsed);
        ThresholdProgress medicalProgress = toProgress(deduction.medicalThresholdAmount(), medicalUsed, medicalRemaining);
        Feedback savedFeedback = syncAiFeedbackService.generateAndSave(userId, sync, deduction);
        SyncFeedback feedback = new SyncFeedback(
                savedFeedback.getType().name(),
                savedFeedback.getTitle(),
                savedFeedback.getMessage(),
                savedFeedback.getExpectedBenefit(),
                savedFeedback.getCreatedAt()
        );

        return new AutoSyncResponse(sync, deduction, cardProgress, medicalProgress, feedback);
    }

    @Transactional
    public SyncResponse syncLast30Days(UUID userId, String connectedId, String organization) {
        UserProfile user = userService.get(userId);

        LocalDate endDate = LocalDate.now(KOREA_ZONE);
        LocalDate startDate = endDate.minusDays(29);

        List<FetchedTransaction> rows = transactionSource.fetchTransactions(
                userId,
                connectedId,
                organization,
                startDate,
                endDate
        );

        int importedCount = 0;
        int duplicateSkippedCount = 0;
        String sourceLabel = resolveSourceLabel();
        for (FetchedTransaction row : rows) {
            String approvalNo = row.approvalNo();
            if (approvalNo == null || approvalNo.isBlank()) {
                continue;
            }

            // Idempotent insert: if approval number already exists for this user/source, skip.
            if (userTransactionRepository.findByUserIdAndSourceAndExternalTxId(userId, sourceLabel, approvalNo).isPresent()) {
                duplicateSkippedCount++;
                continue;
            }

            InternalTransactionCategory internal = categoryMapper.mapFromStoreType(row.storeType());
            ExpenseCategory category = resolveExpenseCategory(internal, row.paymentMethod());

            UserTransaction tx = new UserTransaction(
                    user,
                    category,
                    row.merchantName(),
                    row.storeType(),
                    row.paymentMethod(),
                    row.amount(),
                    row.approvedAt(),
                    null,
                    sourceLabel,
                    approvalNo
            );
            userTransactionRepository.save(tx);
            importedCount++;
        }

        return new SyncResponse(startDate, endDate, importedCount, duplicateSkippedCount);
    }

    private ExpenseCategory resolveExpenseCategory(InternalTransactionCategory category, String paymentMethod) {
        String code;
        switch (category) {
            case MEDICAL -> code = "MEDICAL";
            case TRANSPORT -> code = "PUBLIC_TRANSPORT";
            case FOOD, GENERAL -> code = resolveCardBucket(paymentMethod);
            default -> code = resolveCardBucket(paymentMethod);
        }
        return expenseCategoryRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Expense category not found: " + code));
    }

    private String resolveCardBucket(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "CREDIT_CARD";
        }
        String normalized = paymentMethod.trim().toUpperCase();
        if ("DEBIT".equals(normalized)) {
            return "DEBIT_CARD";
        }
        if ("CASH".equals(normalized)) {
            return "CASH_RECEIPT";
        }
        return "CREDIT_CARD";
    }


    private ThresholdProgress toProgress(long total, long used, long remaining) {
        if (total <= 0) {
            return new ThresholdProgress(total, 0, 0, 1.0d);
        }
        long safeUsed = Math.max(0, Math.min(used, total));
        long safeRemaining = Math.max(0, remaining);
        double ratio = (double) safeUsed / (double) total;
        return new ThresholdProgress(total, safeUsed, safeRemaining, ratio);
    }

    private String resolveSourceLabel() {
        String type = syncSourceProperties.type();
        if (type == null || type.isBlank()) {
            return "CODEF";
        }
        return type.trim().toUpperCase();
    }
}
