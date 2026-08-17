package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SpecialEquipmentAssignmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
public interface SpecialEquipmentAssignmentDetailRepo extends JpaRepository<SpecialEquipmentAssignmentDetail, Integer> {

    List<SpecialEquipmentAssignmentDetail> findAllBySpecialEquipmentAssignmentId(Integer id);
    Long deleteAllBySpecialEquipmentAssignmentId(Integer id);

    BigDecimal countAllByStockTransactionDetailId(Integer itemTestingId);

    @Query(value = "SELECT COUNT(*) as quantityAvailable FROM SpecialEquipment se " +
            "JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "JOIN Item i ON itd.FK_itemId = i.id " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
            "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
            "WHERE sead.id IS NULL " +
            "AND i.id = :itemId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL ", nativeQuery = true)
    BigDecimal countAllAvailableSpecialEquipmentByItemId(@Param("itemId") Integer itemId);

    @Query(value = "SELECT * FROM SpecialEquipmentAssignmentDetail " +
            "WHERE FK_consumerId IS NOT NULL GROUP BY FK_consumerId", nativeQuery = true)
    List<SpecialEquipmentAssignmentDetail> findAllGroupByConsumerId();
}
