package com.livetaxlow.taxfeedback.tax;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRuleSyncLogRepository extends JpaRepository<TaxRuleSyncLog, Long> {
}
