package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Personal on 6/18/2015.
 */
public interface JoDetailRepo extends JpaRepository<JoDetail, Integer> {

    @Transactional
    @Query(value = "SELECT jod.* " +
            "FROM JoDetail jod " +
            "INNER JOIN PurchaseRequestDetail rvd ON jod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE jod.FK_jobOrderId = :joId " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<JoDetail> findByJobOrderId(@Param("joId") Integer id);

    @Transactional
    @Query(value = "SELECT " +
            "jod.* " +
            "FROM JoDetail jod " +
            "INNER JOIN PurchaseRequestDetail rvd ON jod.FK_PurchaseRequestDetailId = rvd.id " +
            "INNER JOIN PurchaseRequest rv ON rvd.FK_PurchaseRequestId = rv.id " +
            "INNER JOIN Item i ON rvd.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON rvd.FK_unitId = u.id " +
            "WHERE jod.FK_jobOrderId = :joId", nativeQuery = true)
    List<JoDetail> findMaterialsByJobOrderId(@Param("joId") Integer id);

    @Transactional
    @Query(value = "SELECT " +
            "jod.* " +
            "FROM JoDetail jod " +
            "INNER JOIN PurchaseRequestDetail rvd ON jod.FK_PurchaseRequestDetailId = rvd.id " +
            "LEFT JOIN Item i ON rvd.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON rvd.FK_unitId = u.id " +
            "WHERE jod.FK_jobOrderId = :joId AND i.code IS NULL " +
            "ORDER BY rvd.id", nativeQuery = true)
    List<JoDetail> findWorksByJobOrderId(@Param("joId") Integer id);

    @Transactional
    public Long deleteByJobOrderId(Integer transId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE JoDetail SET acceptedAmount = :accptdamt WHERE id = :jodId", nativeQuery = true)
    public int updateAcceptedAmountById(@Param("jodId") Integer id, @Param("accptdamt") BigDecimal acceptedAmount);

    @Transactional
    @Query(value = "SELECT jod.* " +
            "FROM JoDetail jod " +
            "INNER JOIN PurchaseRequestDetail rvd ON jod.FK_PurchaseRequestDetailId = rvd.id " +
            "INNER JOIN JobOrder jo ON jod.FK_jobOrderId = jo.id " +
            "WHERE jod.FK_jobOrderId = :joId " +
            "AND jo.FK_documentStatusId = :statusId " +
            "AND (jod.amount - jod.acceptedAmount) != 0 " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<JoDetail> findJoDetailsForJoa(@Param("joId") Integer id, @Param("statusId") Integer statusId);
}