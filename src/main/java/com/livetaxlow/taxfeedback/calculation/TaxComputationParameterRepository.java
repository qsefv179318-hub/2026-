package com.livetaxlow.taxfeedback.calculation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxComputationParameterRepository extends JpaRepository<TaxComputationParameter, Long> {

    List<TaxComputationParameter> findByTaxYear(int taxYear);
}
