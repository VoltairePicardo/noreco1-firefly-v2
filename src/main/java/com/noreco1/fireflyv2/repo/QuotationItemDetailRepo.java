package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationItemDetailRepo extends JpaRepository<QuotationItemDetail, Integer> {
    List<QuotationItemDetail> findAllByQuotationItemQuotationId(Integer quotationId);
    List<QuotationItemDetail> findAllByQuotationItemId(Integer quotationItemId);
    long deleteByQuotationItemQuotationId(Integer qid);
    QuotationItemDetail findOneByQuotationItemPurchaseRequestDetailIdAndIsAwardedTrue(Integer rvDetailId);
    QuotationItemDetail findOneByQuotationItemPurchaseRequestDetailIdAndSupplierAccountNumber(Integer rvDetailId, Integer accountNumber);
}
