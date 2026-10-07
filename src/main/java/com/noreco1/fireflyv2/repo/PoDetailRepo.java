package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Personal on 5/15/2015.
 */
public interface PoDetailRepo extends JpaRepository<PurchaseOrderDetail, Integer> {

    PurchaseOrderDetail findFirstByPurchaseRequestDetailItemIdOrderByIdDesc(Integer itemId);

    @Transactional
    @Query(value = "SELECT pod.* FROM PurchaseOrderDetail pod " +
            "INNER JOIN PurchaseRequestDetail rvd ON pod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE pod.FK_purchaseOrderId = :poId " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<PurchaseOrderDetail> findByPurchaseOrderId(@Param("poId") Integer id);

    @Transactional
    public Long deleteByPurchaseOrderId(Integer transId);

    List<PurchaseOrderDetail> findByPurchaseRequestDetailPurchaseRequestId(Integer rvId);

    @Transactional
    @Query(value = "SELECT pod.* FROM PurchaseOrderDetail pod " +
            "INNER JOIN PurchaseRequestDetail rvd ON pod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE pod.FK_purchaseOrderId = :poId " +
            "ORDER BY rvd.id", nativeQuery = true)
    List<PurchaseOrderDetail> findPoDetailWithItemTestingByPoId(@Param("poId") Integer id);

    List<PurchaseOrderDetail> findAllByPurchaseRequestDetailPurchaseRequestIdOrderByPurchaseOrderCode(Integer purchaseRequestId);
}
