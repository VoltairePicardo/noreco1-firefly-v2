package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CreditCardPurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CreditCardPurchaseRequestRepo extends JpaRepository<CreditCardPurchaseRequest, Integer> {

    @Query(value = "SELECT e.code FROM CreditCardPurchaseRequest e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    CreditCardPurchaseRequest findFirstByOrderByIdAsc();

    List<CreditCardPurchaseRequest> findAllByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    List<CreditCardPurchaseRequest> findAllByVoucherDateBetweenAndDocumentStatusId(Date from, Date to, Integer statusId);

    CreditCardPurchaseRequest findOneByTransactionId(Integer transactionId);

    List<CreditCardPurchaseRequest> findAllByBatchId(Integer batchId);

    Page<CreditCardPurchaseRequest> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}
