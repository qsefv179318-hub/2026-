package com.livetaxlow.taxfeedback.payment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(attributePaths = "category")
    List<Payment> findByUserIdAndApprovedAtBetweenOrderByApprovedAtDesc(UUID userId, Instant from, Instant to);

    @EntityGraph(attributePaths = "category")
    List<Payment> findByUserIdOrderByApprovedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "category")
    Optional<Payment> findByTossOrderId(String tossOrderId);

    @EntityGraph(attributePaths = "category")
    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("""
        select p.category.code as categoryCode, sum(p.amount) as totalAmount
        from Payment p
        where p.user.id = :userId
          and p.approvedAt >= :from
          and p.approvedAt < :to
          and p.deductible = true
        group by p.category.code
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
