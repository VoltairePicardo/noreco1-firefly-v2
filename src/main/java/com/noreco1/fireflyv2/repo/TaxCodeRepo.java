package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TaxCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxCodeRepo extends JpaRepository<TaxCode, Integer> {
    TaxCode findByAccountId(Integer accountId);
}
