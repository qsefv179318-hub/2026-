package com.livetaxlow.taxfeedback.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossWebhookEventRepository extends JpaRepository<TossWebhookEvent, UUID> {

    Optional<TossWebhookEvent> findByEventId(String eventId);
}
