package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Tri-Nvent on 12/4/2019.
 */
public interface SpecialEquipmentRepo extends JpaRepository<SpecialEquipment, Integer> {

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "INNER JOIN Item i ON itd.FK_itemId = i.id " +
            "INNER JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "INNER JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND se.FK_specialEquipmentTypeId = :specialEquipmentType " +
            "AND sead.id IS NULL " +
            "ORDER BY mt.date, mt.id, se.id " +
            "LIMIT :noOfTurnOnOrders ",
            nativeQuery = true)
    List<SpecialEquipment> findAllDefaultForSpecialEquipmentAssignment(@Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                                       @Param("specialEquipmentType") Integer specialEquipmentType,
                                                                       @Param("noOfTurnOnOrders") Integer noOfTurnOnOrders);

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "INNER JOIN Item i ON itd.FK_itemId = i.id " +
            "INNER JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "INNER JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND se.FK_specialEquipmentTypeId = :specialEquipmentType " +
            "AND sead.id IS NULL " +
            "ORDER BY mt.date, mt.id, se.id  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipment se " +
                    "INNER JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
                    "INNER JOIN Item i ON itd.FK_itemId = i.id " +
                    "INNER JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
                    "INNER JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
                    "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "AND se.FK_specialEquipmentTypeId = :specialEquipmentType " +
                    "AND sead.id IS NULL",
            nativeQuery = true)
    Page<SpecialEquipment> findAllSpecialEquipmentAssignmentPaged(@Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                                  @Param("specialEquipmentType") Integer specialEquipmentType,
                                                                  Pageable pageable);

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "INNER JOIN Item i ON itd.FK_itemId = i.id " +
            "INNER JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "INNER JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND se.FK_specialEquipmentTypeId = :specialEquipmentType " +
            "AND sead.id IS NULL " +
            "AND se.serialNo LIKE :query " +
            "ORDER BY mt.date, mt.id, se.id  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipment se " +
                    "INNER JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
                    "INNER JOIN Item i ON itd.FK_itemId = i.id " +
                    "INNER JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
                    "INNER JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
                    "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "AND se.FK_specialEquipmentTypeId = :specialEquipmentType " +
                    "AND sead.id IS NULL " +
                    "AND se.serialNo LIKE :query ",
            nativeQuery = true)
    Page<SpecialEquipment> findAllSpecialEquipmentAssignmentByQueryPaged(@Param("query") String query,
                                                                         @Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                                         @Param("specialEquipmentType") Integer specialEquipmentType,
                                                                         Pageable pageable);

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_specialEquipmentId = se.id " +
            "INNER JOIN StockTransactionDetail std ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN ItemStock ist ON ist.id = std.FK_itemStockId " +
            "INNER JOIN Item i ON i.id = ist.FK_itemId " +
            "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
            "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
            "WHERE sead.id IS NULL " +
            "AND i.id = :itemId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            "ORDER BY mt.date, mt.id, se.id " +
            "LIMIT :noOfItems ",
            nativeQuery = true)
    List<SpecialEquipment> findAllDefaultForSpecialEquipmentAssignmentNoTurnOn(@Param("itemId") Integer itemId,
                                                                               @Param("noOfItems") Integer noOfItems);

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_specialEquipmentId = se.id " +
            "INNER JOIN StockTransactionDetail std ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN ItemStock ist ON ist.id = std.FK_itemStockId " +
            "INNER JOIN Item i ON i.id = ist.FK_itemId " +
            "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
            "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "WHERE sead.id IS NULL " +
            "AND i.id = :itemId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            "ORDER BY mt.date, mt.id, se.id  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipment se " +
                    "INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_specialEquipmentId = se.id " +
                    "INNER JOIN StockTransactionDetail std ON std.id = stdsn.FK_stockTransactionDetailId " +
                    "INNER JOIN ItemStock ist ON ist.id = std.FK_itemStockId " +
                    "INNER JOIN Item i ON i.id = ist.FK_itemId " +
                    "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
                    "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
                    "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
                    "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
                    "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
                    "WHERE sead.id IS NULL " +
                    "AND i.id = :itemId " +
                    "AND cset.id IS NOT NULL " +
                    "OR mtd.id IS NOT NULL " +
                    "OR tt.id IS NOT NULL ",
            nativeQuery = true)
    Page<SpecialEquipment> findAllSpecialEquipmentAssignmentNoConnectOrderPaged(@Param("itemId") Integer itemId,
                                                                                Pageable pageable);

    @Query(value = "SELECT se.* FROM SpecialEquipment se " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_specialEquipmentId = se.id " +
            "INNER JOIN StockTransactionDetail std ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN ItemStock ist ON ist.id = std.FK_itemStockId " +
            "INNER JOIN Item i ON i.id = ist.FK_itemId " +
            "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
            "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
            "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
            "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
            "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
            "WHERE sead.id IS NULL " +
            "AND i.id = :itemId " +
            "AND se.serialNo LIKE :query " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            "ORDER BY mt.date, mt.id, se.id  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipment se " +
                    "INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_specialEquipmentId = se.id " +
                    "INNER JOIN StockTransactionDetail std ON std.id = stdsn.FK_stockTransactionDetailId " +
                    "INNER JOIN ItemStock ist ON ist.id = std.FK_itemStockId " +
                    "INNER JOIN Item i ON i.id = ist.FK_itemId " +
                    "LEFT JOIN MeterTestingDetail mtd ON se.id = mtd.FK_specialEquipmentId " +
                    "LEFT JOIN MeterTesting mt ON mtd.FK_meterTestingId = mt.id " +
                    "LEFT JOIN CommonSpecialEquipmentTesting cset ON se.id = cset.FK_specialEquipmentId " +
                    "LEFT JOIN TransformerTesting tt ON se.id = tt.FK_specialEquipmentId " +
                    "LEFT JOIN SpecialEquipmentAssignmentDetail sead ON se.id = sead.FK_specialEquipmentId " +
                    "WHERE sead.id IS NULL " +
                    "AND i.id = :itemId " +
                    "AND se.serialNo LIKE :query " +
                    "AND cset.id IS NOT NULL " +
                    "OR mtd.id IS NOT NULL " +
                    "OR tt.id IS NOT NULL ",
            nativeQuery = true)
    Page<SpecialEquipment> findAllSpecialEquipmentAssignmentNoConnectOrderByQueryPaged(@Param("query") String query,
                                                                                       @Param("itemId") Integer itemId,
                                                                                       Pageable pageable);

    SpecialEquipment findBySerialNo(String serialNo);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "SpecialEquipmentAssignment.id as id, " +
            "`date`, " +
            "location, " +
            "'' as remarks, " +
            "SpecialEquipmentAssignmentDetail.FK_consumerId as consumerId," +
            "createdAt " +
            "FROM SpecialEquipmentAssignment " +
            "JOIN SpecialEquipmentAssignmentDetail ON SpecialEquipmentAssignment.id = FK_specialEquipmentAssignmentId " +
            "WHERE FK_specialEquipmentId = :id " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "StockRelease.code as id, " +
            "voucherDate as `date`, " +
            "'' location, " +
            "'' as remarks, " +
            "NULL as consumerId," +
            "createdAt " +
            "FROM StockRelease " +
            "WHERE StockRelease.FK_transactionId " +
            "IN ( " +
            "        SELECT " +
            "    FK_stockTransactionId " +
            "    FROM StockTransactionDetailSerialNo " +
            "    JOIN StockTransactionDetail ON StockTransactionDetailSerialNo.FK_stockTransactionDetailId = StockTransactionDetail.id " +
            "    WHERE FK_specialEquipmentId = :id " +
            ") " +
            "" +
            "UNION " +
            " " +
            "SELECT " +
            "id, " +
            "`date`, " +
            "'' as location, " +
            "'' as remarks, " +
            "NULL as consumerId," +
            "createdAt " +
            " FROM SpecialEquipmentRepair " +
            " WHERE SpecialEquipmentRepair.id " +
            " IN ( " +
            "     SELECT " +
            "     FK_specialEquipmentRepairId " +
            "     FROM SpecialEquipmentRepairDetail " +
            "     WHERE FK_specialEquipmentId = :id " +
            " ) " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            "id, " +
            "`date`, " +
            "'' as location, " +
            "'' as remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM MeterTesting " +
            "WHERE MeterTesting.id " +
            "IN ( " +
            "    SELECT " +
            "    FK_meterTestingId " +
            "    FROM MeterTestingDetail " +
            "    WHERE FK_specialEquipmentId = :id " +
            ") " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "controlNo as id, " +
            "`date`, " +
            "'' location, " +
            "remarks, " +
            "NULL as consumerId," +
            "createdAt " +
            "FROM TransformerTesting " +
            "WHERE TransformerTesting.FK_specialEquipmentId = :id " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "TransformerWindingResistanceTest.id as id, " +
            "`date`, " +
            "'' location, " +
            "remarks, " +
            "NULL as consumerId," +
            "createdAt " +
            "FROM TransformerWindingResistanceTest " +
            "JOIN CommonSpecialEquipmentTesting ON FK_commonSpecialEquipmentTestingId = CommonSpecialEquipmentTesting.id " +
            "WHERE FK_specialEquipmentId = :id " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "controlNo as id, " +
            "`date`, " +
            "'' location, " +
            "remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM TurnsRatioTest " +
            "JOIN TransformerTesting ON FK_transformerTestingId = TransformerTesting.id " +
            "WHERE FK_specialEquipmentId = :id  " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "MaterialCreditTicket.code as id, " +
            "voucherDate as `date`, n" +
            "'' location, " +
            "'' as remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM MaterialCreditTicket " +
            "WHERE MaterialCreditTicket.FK_transactionId " +
            "IN (" +
            "        SELECT " +
            "    FK_stockTransactionId " +
            "    FROM StockTransactionDetailSerialNo " +
            "    JOIN StockTransactionDetail ON StockTransactionDetailSerialNo.FK_stockTransactionDetailId = StockTransactionDetail.id" +
            "    WHERE FK_specialEquipmentId = :id " +
            ") " +
            "UNION " +
            " " +
            "SELECT " +
            "MaterialSalvageTicket.code as id, " +
            "voucherDate as `date`, " +
            "'' location, " +
            "'' as remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM MaterialSalvageTicket " +
            "WHERE MaterialSalvageTicket.FK_transactionId " +
            "IN ( " +
            "        SELECT " +
            "    FK_stockTransactionId " +
            "    FROM StockTransactionDetailSerialNo " +
            "    JOIN StockTransactionDetail ON StockTransactionDetailSerialNo.FK_stockTransactionDetailId = StockTransactionDetail.id " +
            "    WHERE FK_specialEquipmentId = :id " +
            ") " +
            " " +
            "UNION " +
            " " +
            "SELECT " +
            "id, " +
            "`date`, " +
            "'' location, " +
            "'' remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM CommonSpecialEquipmentTesting " +
            "WHERE FK_specialEquipmentId = :id " +
            " " +
            "UNION " +
            " " +
            " " +
            "SELECT " +
            "TurnsRatioPrimaryToSecondaryTest.id as id, " +
            "`date`, " +
            "'' location, " +
            "'' remarks, " +
            "NULL as consumerId, " +
            "createdAt " +
            "FROM TurnsRatioPrimaryToSecondaryTest " +
            "JOIN CommonSpecialEquipmentTesting ON FK_commonSpecialEquipmentTestingId = CommonSpecialEquipmentTesting.id " +
            "WHERE FK_specialEquipmentId = :id " +
            ") transactions " +
            " " +
            "ORDER BY `date`", nativeQuery = true)
    List<Object []> findSpecialEquipmentTransactionsByEquipmentId(@Param("id") Integer id);

    Page<SpecialEquipment> findAllBySerialNoContainingIgnoreCase(String query, Pageable pageable);

}
