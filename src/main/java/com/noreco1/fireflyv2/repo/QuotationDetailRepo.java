package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuotationDetailRepo extends JpaRepository<QuotationDetail, Integer> {

    @Query(value = "SELECT * FROM QuotationDetail qd WHERE qd.FK_quotationId = :quotationId ORDER BY qd.FK_PurchaseRequestDetailId ",
            nativeQuery = true)
    List<QuotationDetail> findAllQuotationDetailByQuotationId(@Param("quotationId") Integer quotationId);

    @Transactional
    Long deleteByQuotationId(Integer transId);

    List<QuotationDetail> findByPurchaseRequestDetailPurchaseRequestId(Integer id);

    QuotationDetail findFirstBySupplierIdAndPurchaseRequestDetailIdAndQuotationDocumentStatusIdNotOrderByQuotationCreatedAtDesc(Integer supplierId,
                                                                                                                Integer PurchaseRequestDetailId,
                                                                                                                Integer statusId);
    QuotationDetail findFirstByPurchaseRequestDetailId(Integer PurchaseRequestDetailId);
}
