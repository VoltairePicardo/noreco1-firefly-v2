package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationItemRepo extends JpaRepository<QuotationItem, Integer> {
    List<QuotationItem> findAllByQuotationId(Integer id);
    long deleteByQuotationId(Integer qid);

    @Query("SELECT DISTINCT qi.purchaseRequestDetail.purchaseRequest.code " +
           "FROM QuotationItem qi " +
           "WHERE qi.quotation.id = :quotationId AND qi.purchaseRequestDetail.purchaseRequest IS NOT NULL")
    List<String> findDistinctPrCodesByQuotationId(@Param("quotationId") Integer quotationId);
}
