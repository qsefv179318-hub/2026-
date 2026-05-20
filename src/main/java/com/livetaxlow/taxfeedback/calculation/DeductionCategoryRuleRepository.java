package com.livetaxlow.taxfeedback.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeductionCategoryRuleRepository extends JpaRepository<DeductionCategoryRule, Long> {

    List<DeductionCategoryRule> findByTaxYearOrderByThresholdFillPriorityAsc(int taxYear);
}
