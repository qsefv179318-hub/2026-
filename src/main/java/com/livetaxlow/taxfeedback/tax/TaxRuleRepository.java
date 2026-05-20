package com.livetaxlow.taxfeedback.tax;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    @EntityGraph(attributePaths = "category")
    List<TaxRule> findByTaxYear(int taxYear);

    @EntityGraph(attributePaths = "category")
    Optional<TaxRule> findByTaxYearAndCategory_Code(int taxYear, String categoryCode);
}
