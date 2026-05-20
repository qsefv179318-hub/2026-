package com.livetaxlow.taxfeedback.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<PaymentOrder> findByOrderId(String orderId);
}
