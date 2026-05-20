package com.livetaxlow.taxfeedback.finance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalFinanceLinkRepository extends JpaRepository<ExternalFinanceLink, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<ExternalFinanceLink> findByUserIdAndProviderAndActiveTrue(UUID userId, String provider);

    List<ExternalFinanceLink> findByUserIdAndActiveTrue(UUID userId);
}
