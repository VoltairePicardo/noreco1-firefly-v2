package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PoDetail;
import com.noreco1.fireflyv2.controller.response.reports.PODetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Personal on 5/15/2015.
 */
public interface PoDetailRepo extends JpaRepository<PoDetail, Integer> {

    PoDetail findFirstByPurchaseRequestDetailItemIdOrderByIdDesc(Integer itemId);

    @Transactional
    @Query(value = "SELECT pod.* FROM PoDetail pod " +
            "INNER JOIN PurchaseRequestDetail rvd ON pod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE pod.FK_purchaseOrderId = :poId " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<PoDetail> findByPurchaseOrderId(@Param("poId") Integer id);

    @Transactional
    public Long deleteByPurchaseOrderId(Integer transId);

    List<PoDetail> findByPurchaseRequestDetailPurchaseRequestId(Integer rvId);

    @Transactional
    @Query(value = "SELECT pod.* FROM PoDetail pod " +
            "INNER JOIN PurchaseRequestDetail rvd ON pod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE pod.FK_purchaseOrderId = :poId " +
            "ORDER BY rvd.id", nativeQuery = true)
    List<PoDetail> findPoDetailWithItemTestingByPoId(@Param("poId") Integer id);

    List<PoDetail> findAllByPurchaseRequestDetailPurchaseRequestIdOrderByPurchaseOrderCode(Integer purchaseRequestId);
}
