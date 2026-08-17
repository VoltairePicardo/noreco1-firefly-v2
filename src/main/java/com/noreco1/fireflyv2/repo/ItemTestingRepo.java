package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemTesting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
public interface ItemTestingRepo extends JpaRepository<ItemTesting, Integer> {

    Page<ItemTesting> findAllByDateBetweenOrderByDate(Date startDate, Date endDate, Pageable pageable);

    @Query(value = "SELECT  " +
            "*  " +
            "FROM ( " +
            " " +
            "  SELECT  " +
            "  it.date AS historyDate, " +
            "  'Received' AS transactionType, " +
            "  NULL AS referenceNumber, " +
            "  NULL AS crew, " +
            "  itd.remarks AS remark, " +
            "  u.fullName AS TransactBy, " +
            "  it.createdAt AS transactionDate " +
            "  FROM ItemTesting it " +
            "  INNER JOIN ItemTestingDetail itd ON itd.FK_itemTestingId = it.id " +
            "  INNER JOIN SpecialEquipment se ON se.FK_itemTestingDetailId = itd.id " +
            "  INNER JOIN User u ON u.id = it.FK_createdByUserId " +
            "  WHERE se.serialNo = :serialNumber " +
            "   " +
            "  UNION  " +
            "   " +
            "  SELECT  " +
            "  mt.date AS historyDate, " +
            "  'Tested' AS transactionType, " +
            "  NULL AS referenceNumber, " +
            "  NULL AS crew, " +
            "  mtd.findings AS remark, " +
            "  u.fullName AS TransactBy, " +
            "  mt.createdAt AS transactionDate " +
            "  FROM ItemTesting it " +
            "  INNER JOIN ItemTestingDetail itd ON itd.FK_itemTestingId = it.id " +
            "  INNER JOIN SpecialEquipment se ON se.FK_itemTestingDetailId = itd.id " +
            "  INNER JOIN MeterTestingDetail mtd ON mtd.FK_specialEquipmentId = se.id " +
            "  INNER JOIN MeterTesting mt ON mt.id = mtd.FK_meterTestingId " +
            "  INNER JOIN User u ON u.id = mt.FK_createdByUserId " +
            "  WHERE se.serialNo = :serialNumber " +
            "   " +
            "  UNION  " +
            "   " +
            "  SELECT  " +
            "  sr.voucherDate AS historyDate, " +
            "  'Released' AS transactionType, " +
            "  sr.code AS referenceNumber, " +
            "  NULL AS crew, " +
            "  sr.description AS remark, " +
            "  u.fullName AS TransactBy, " +
            "  sr.createdAt AS transactionDate " +
            "  FROM StockRelease sr " +
            "  INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "  INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "  INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_stockTransactionDetailId = sttd.id " +
            "  INNER JOIN User u ON u.id = sr.FK_createdByUserId " +
            "  WHERE stdsn.serialNo = :serialNumber " +
            "   " +
            "  UNION  " +
            "   " +
            "  SELECT  " +
            "  sr.voucherDate AS historyDate, " +
            "  'Issued' AS transactionType, " +
            "  sr.code AS referenceNumber, " +
            "  NULL AS crew, " +
            "  sr.description AS remark, " +
            "  u.fullName AS TransactBy, " +
            "  sr.createdAt AS transactionDate " +
            "  FROM StockRelease sr " +
            "  INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "  INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "  INNER JOIN StockTransactionDetailSerialNo stdsn ON stdsn.FK_stockTransactionDetailId = sttd.id " +
            "  INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
            "  INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
            "  INNER JOIN User u ON u.id = sr.FK_createdByUserId " +
            "  WHERE stdsn.serialNo = :serialNumber " +
            " " +
            ") AS ItemHistory " +
            "ORDER BY ItemHistory.historyDate ", nativeQuery = true)
    List<Object[]> getItemHistory(@Param("serialNumber") String serialNumber);

}
