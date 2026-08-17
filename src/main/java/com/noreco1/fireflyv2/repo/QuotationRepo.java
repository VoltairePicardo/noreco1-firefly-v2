package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface QuotationRepo extends JpaRepository<Quotation, Integer> {

    Quotation findFirstByOrderByIdDesc();

    @Query(value = "SELECT e.code FROM Quotation e WHERE year = :year  AND code LIKE '%SOQ%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestQuotationCodeByYear(@Param("year") Integer year);

    Quotation findOneByTransactionId(Integer id);

    List<Quotation> findByDocumentStatusIdAndDateBetween(Integer statusId, Date from, Date to);
//    List<Quotation> findByDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);

    Quotation findFirstByOrderByIdAsc();

    List<Quotation> findByDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    Quotation findFirstByApprovedByGeneralManagerNotNullOrderByIdDesc();

    Quotation findFirstByApprovedByFinanceManagerNotNullOrderByIdDesc();

    List<Quotation> findAllByPurchaseRequestIdOrderByCode(Integer purchaseRequestId);

    Page<Quotation> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}
