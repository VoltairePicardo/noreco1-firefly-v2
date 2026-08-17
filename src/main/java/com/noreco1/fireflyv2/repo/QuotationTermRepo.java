package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationTermRepo extends JpaRepository<QuotationTerm, Integer> {
    List<QuotationTerm> findAllByQuotationId(Integer id);
    long deleteByQuotationId(Integer qid);

    @Query(value = "SELECT qterm.* FROM QuotationItem qitem " +
            "JOIN QuotationTerm qterm ON qitem.FK_quotationId = qterm.FK_quotationId " +
            "WHERE FK_purchaseRequestDetailId = :rvDetailId AND qterm.FK_supplierId = :supplierId " +
            "GROUP BY qterm.id LIMIT 1;", nativeQuery = true)
    List<QuotationTerm> findByRvDetailAndSupplier(@Param("rvDetailId") Integer rvDetailId, @Param("supplierId") Integer supplierId);
}
