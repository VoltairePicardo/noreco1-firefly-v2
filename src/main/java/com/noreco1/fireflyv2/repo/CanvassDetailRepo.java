package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Canvass;
import com.noreco1.fireflyv2.model.CanvassDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Personal on 5/12/2015.
 */
public interface CanvassDetailRepo extends JpaRepository<CanvassDetail, Integer> {
    @Transactional
    @Query(value = "SELECT cnvsd.*, rvd.quantity, i.code, i.description, u.code, rv.code FROM canvassDetail cnvsd " +
            "INNER JOIN PurchaseRequestDetail rvd ON cnvsd.FK_PurchaseRequestDetailId = rvd.id " +
            "INNER JOIN PurchaseRequest rv ON rvd.FK_PurchaseRequestId = rv.id " +
            "LEFT JOIN item i ON rvd.FK_itemId = i.id " +
            "LEFT JOIN unitMeasure u ON rvd.FK_unitId = u.id " +
            "WHERE cnvsd.FK_canvassId = :canvassId " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<CanvassDetail> findByCanvassId(@Param("canvassId")Integer id);

    public Long deleteByCanvassId(Integer transId);

    List<CanvassDetail> findByPurchaseRequestDetailPurchaseRequestId(Integer rivId);
    CanvassDetail findBySupplierIdAndPurchaseRequestDetailId(Integer supplierId, Integer PurchaseRequestDetailId);

    @Transactional
    @Query(value = "SELECT " +
            "CanvassDetail.* " +
            "FROM PurchaseRequest rv " +
            "JOIN PurchaseRequestDetail ON rv.id = PurchaseRequestDetail.FK_PurchaseRequestId " +
            "JOIN CanvassDetail ON PurchaseRequestDetail.id = CanvassDetail.FK_PurchaseRequestDetailId " +
            "WHERE PurchaseRequestDetail.id not in (select PoDetail.FK_PurchaseRequestDetailId from PoDetail JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id AND PurchaseOrder.FK_documentStatusId != 26) " +
            "AND PurchaseRequestDetail.id not in (select JoDetail.FK_PurchaseRequestDetailId from JoDetail JOIN JobOrder ON JoDetail.FK_jobOrderId = JobOrder.id AND JobOrder.FK_documentStatusId != 26) " +
            "AND rv.id = :rvId " +
            "GROUP BY PurchaseRequestDetail.id ORDER BY PurchaseRequestDetail.id", nativeQuery = true)
    List<CanvassDetail> findForQoutationByPurchaseRequestId(@Param("rvId") Integer rivId);

    CanvassDetail findFirstByCanvassId(Integer id);

    List<CanvassDetail> findAllByPurchaseRequestDetailPurchaseRequestIdOrderByCanvassCode(Integer purchaseRequestId);

}
