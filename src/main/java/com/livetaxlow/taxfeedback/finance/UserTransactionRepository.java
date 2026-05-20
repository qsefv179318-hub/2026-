package com.livetaxlow.taxfeedback.finance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTransactionRepository extends JpaRepository<UserTransaction, UUID> {

    Optional<UserTransaction> findByUserIdAndSourceAndExternalTxId(UUID userId, String source, String externalTxId);

    void deleteByUserIdAndSource(UUID userId, String source);

    List<UserTransaction> findByUserIdOrderByApprovedAtDesc(UUID userId);

    List<UserTransaction> findTop5ByUserIdOrderByApprovedAtDesc(UUID userId);

    @Query("""
        select t.category.code as categoryCode, sum(t.amount) as totalAmount
        from UserTransaction t
        where t.user.id = :userId
          and t.approvedAt >= :from
          and t.approvedAt < :to
        group by t.category.code
    """)
    List<CategoryAmountView> summarizeByCategory(
            @Param("userId") UUID userId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    interface CategoryAmountView {
        String getCategoryCode();
        Long getTotalAmount();
    }
}
