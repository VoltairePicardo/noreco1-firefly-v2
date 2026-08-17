package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CreditCardPurchaseRequestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CreditCardPurchaseRequestBatchRepo extends JpaRepository<CreditCardPurchaseRequestBatch, Integer> {

    List<CreditCardPurchaseRequestBatch> findAllByOfficeId(Integer officeId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT ccprb.* " +
            "FROM CreditCardPurchaseRequestBatch ccprb " +
            "WHERE EXISTS ( " +
            "    SELECT 1 " +
            "    FROM CreditCardPurchaseRequest ccpr " +
            "    WHERE ccpr.FK_creditCardPurchaseRequestBatchId = ccprb.id " +
            "    AND ccpr.FK_expenseAccountId IS NOT NULL " +
            ") " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 " +
            "    FROM JournalVoucher jv " +
            "    WHERE jv.FK_creditCardPurchaseRequestBatchId = ccprb.id " +
            ") ", nativeQuery = true)
    List<CreditCardPurchaseRequestBatch> findAllBatchesForJv();

}
