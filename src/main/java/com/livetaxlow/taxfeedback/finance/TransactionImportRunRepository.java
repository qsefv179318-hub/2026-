package com.livetaxlow.taxfeedback.finance;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionImportRunRepository extends JpaRepository<TransactionImportRun, UUID> {
}
