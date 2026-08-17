package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StatementOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatementOfAccountRepo extends JpaRepository<StatementOfAccount, Integer> {
    StatementOfAccount findByDocId(Integer docId);
}
