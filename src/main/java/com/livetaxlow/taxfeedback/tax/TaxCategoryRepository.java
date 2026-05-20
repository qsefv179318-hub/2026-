package com.livetaxlow.taxfeedback.tax;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxCategoryRepository extends JpaRepository<TaxCategory, Long> {

    Optional<TaxCategory> findByCode(String code);
}
