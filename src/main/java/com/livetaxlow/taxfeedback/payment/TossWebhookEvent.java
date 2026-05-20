package com.livetaxlow.taxfeedback.payment;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "toss_webhook_events")
public class TossWebhookEvent {

    @Id
    private UUID id;

    private String eventId;

    @Column(nullable = false)
    private String eventType;

    private String paymentKey;

    private String orderId;

    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode rawPayload;

    @Column(nullable = false)
    private boolean processed;

    @Column(nullable = false)
    private Instant receivedAt;

    protected TossWebhookEvent() {
    }

    public TossWebhookEvent(String eventId, String eventType, String paymentKey, String orderId, String status, JsonNode rawPayload) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.eventType = eventType;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.rawPayload = rawPayload;
        this.processed = false;
        this.receivedAt = Instant.now();
    }

    public void markProcessed() {
        this.processed = true;
    }
}
