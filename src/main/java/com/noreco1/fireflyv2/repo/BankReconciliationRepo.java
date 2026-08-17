package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BankReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Personal on 6/30/2015.
 */
public interface BankReconciliationRepo extends JpaRepository<BankReconciliation, Integer> {
    @Query(value = "SELECT * FROM BankReconciliation WHERE documentId = :documentId AND FK_transactionId = :transId", nativeQuery = true)
    public BankReconciliation findOneByTransactionIdAndDocumentId(@Param("transId") Integer transId, @Param("documentId")Integer documentId);
}
