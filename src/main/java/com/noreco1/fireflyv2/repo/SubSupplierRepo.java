package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SubSupplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubSupplierRepo extends JpaRepository<SubSupplier, Integer> {

    SubSupplier findBySupplierIdAndStlIdEquals(Integer supplierId, String stlId);
    List<SubSupplier> findAllBySupplierIdOrderByParticipantName(Integer supplierId);
    Page<SubSupplier> findAllBySupplierIdOrderByParticipantName(Integer supplierId, Pageable pageable);
    Page<SubSupplier> findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrParticipantNameContainingIgnoreCaseOrTradeNameContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseOrderByParticipantName(String q1,
                                                                                                                                                                    String q2,
                                                                                                                                                                    String q3,
                                                                                                                                                                    String q4, String q5, Pageable pageable);
    Page<SubSupplier> findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrParticipantNameContainingIgnoreCaseOrTradeNameContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseAndSupplierIdOrderByParticipantName(String q1,
                                                                                                                                                                               String q2,
                                                                                                                                                                               String q3,
                                                                                                                                                                               String q4, String q5, Integer suppId, Pageable pageable);
    SubSupplier findByStlId(String stlId);
}
