package com.livetaxlow.taxfeedback.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.livetaxlow.taxfeedback.user.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "external_finance_links")
public class ExternalFinanceLink {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String organization;

    @Column(nullable = false)
    private String connectedId;

    @Column(nullable = false)
    private String clientType;

    private String accountRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ExternalFinanceLink() {
    }

    public ExternalFinanceLink(
            UserProfile user,
            String provider,
            String organization,
            String connectedId,
            String clientType,
            String accountRef,
            JsonNode metadata
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.provider = provider;
        this.organization = organization;
        this.connectedId = connectedId;
        this.clientType = clientType;
        this.accountRef = accountRef;
        this.metadata = metadata;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UserProfile getUser() {
        return user;
    }

    public String getProvider() {
        return provider;
    }

    public String getOrganization() {
        return organization;
    }

    public String getConnectedId() {
        return connectedId;
    }

    public String getClientType() {
        return clientType;
    }

    public String getAccountRef() {
        return accountRef;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public boolean isActive() {
        return active;
    }
}
