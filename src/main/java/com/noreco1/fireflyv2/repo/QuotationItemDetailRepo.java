package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationItemDetail;
import com.noreco1.fireflyv2.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationItemDetailRepo extends JpaRepository<QuotationItemDetail, Integer> {
    List<QuotationItemDetail> findAllByQuotationItemQuotationId(Integer quotationId);

    @Query("SELECT DISTINCT qid.supplier FROM QuotationItemDetail qid WHERE qid.quotationItem.quotation.id = :quotationId AND qid.supplier IS NOT NULL")
    List<Supplier> findDistinctSuppliersByQuotationId(@Param("quotationId") Integer quotationId);
    List<QuotationItemDetail> findAllByQuotationItemId(Integer quotationItemId);
    long deleteByQuotationItemQuotationId(Integer qid);
    QuotationItemDetail findOneByQuotationItemPurchaseRequestDetailIdAndIsAwardedTrue(Integer rvDetailId);
    QuotationItemDetail findOneByQuotationItemPurchaseRequestDetailIdAndSupplierAccountNumber(Integer rvDetailId, Integer accountNumber);
}
