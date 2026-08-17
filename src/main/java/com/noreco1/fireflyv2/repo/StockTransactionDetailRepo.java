package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockTransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface StockTransactionDetailRepo extends JpaRepository<StockTransactionDetail, Integer> {
    ArrayList<StockTransactionDetail> findByStockTransactionTransactionId(Integer transId);

    void deleteByStockTransactionTransactionId(Integer transId);

//    @Query(value = "SELECT SUM(debit), SUM(credit), id, title, code, hasSL " +
//            "FROM (SELECT IF(type = 2, std.totalCost, 0) AS debit, " +
//            "IF(type = 1, std.totalCost, 0) AS credit, " +
//            "a.id, a.title, a.code, a.hasSL, std.`type` " +
//            "FROM StockTransactionDetail std " +
//            "INNER JOIN StockTransaction st ON st.id = std.FK_stockTransactionId " +
//            "INNER JOIN ItemStock ii ON ii.id = std.FK_itemStockId " +
//            "INNER JOIN Item i ON i.id = ii.FK_itemId " +
//            "INNER JOIN Account a ON a.id = i.FK_expenseAccountId " +
//            "WHERE st.FK_transactionId = :transId " +
//            "UNION " +
//            "SELECT IF(type = 2, 0, std.totalCost) AS debit, " +
//            "IF(type = 1, 0, std.totalCost) AS credit, " +
//            "a.id, a.title, a.code, a.hasSL, std.`type` " +
//            "FROM StockTransactionDetail std " +
//            "INNER JOIN StockTransaction st ON st.id = std.FK_stockTransactionId " +
//            "INNER JOIN ItemStock ii ON ii.id = std.FK_itemStockId " +
//            "INNER JOIN Item i ON i.id = ii.FK_itemId " +
//            "INNER JOIN Account a ON a.id = i.FK_assetAccountId " +
//            "WHERE st.FK_transactionId = :transId) T1 GROUP BY id ORDER BY debit DESC, credit, code", nativeQuery = true)
//    List<Object[]> findGLAccountEntriesByTransactionId(@Param("transId") Integer transId);

    @Query(value = "SELECT " +
            "SUM(debit), " +
            "SUM(credit), " +
            "id, " +
            "title, " +
            "code, " +
            "hasSL " +
            "FROM ( " +
            " " +
            "  SELECT  " +
            "  IF(type = 2, StockTransactionDetail.totalCost, 0) AS debit,  " +
            "  IF(type = 1, StockTransactionDetail.totalCost, 0) AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_debitAccountId  " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            "   " +
            "  UNION " +
            "   " +
            "  SELECT  " +
            "  IF(type = 2, 0, StockTransactionDetail.totalCost) AS debit,  " +
            "  IF(type = 1, 0, StockTransactionDetail.totalCost) AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_creditAccountId " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            " " +
            ") T1 GROUP BY id ORDER BY debit DESC, credit, code ", nativeQuery = true)
    List<Object[]> findGLAccountEntriesByTransactionId(@Param("transId") Integer transId);

//    @Query(value = "SELECT SUM(debit), SUM(credit), id, title, code, hasSL " +
//            "FROM (SELECT IF(type = 2, IF(NOT itd.isUsable, std.totalCost, 0), IF(itd.isUsable, std.totalCost, 0)) AS debit, " +
//            "IF(type = 1, IF(NOT itd.isUsable, std.totalCost, 0), IF(itd.isUsable, std.totalCost, 0)) AS credit, " +
//            "a.id, a.title, a.code, a.hasSL, std.`type` " +
//            "FROM StockTransactionDetail std " +
//            "INNER JOIN StockTransaction st ON st.id = std.FK_stockTransactionId " +
//            "INNER JOIN ItemStock ii ON ii.id = std.FK_itemStockId " +
//            "INNER JOIN Item i ON i.id = ii.FK_itemId " +
//            "INNER JOIN Account a ON a.id = i.FK_expenseAccountId " +
//            "INNER JOIN ItemTransactionDetail itd ON itd.FK_itemStockId = std.FK_itemStockId " +
//            "WHERE st.FK_transactionId = :transId " +
//            "UNION " +
//            "SELECT IF(type = 2, IF(itd.isUsable, std.totalCost, 0), IF(NOT itd.isUsable, std.totalCost, 0)) AS debit, " +
//            "IF(type = 1, IF(itd.isUsable, std.totalCost, 0), IF(NOT itd.isUsable, std.totalCost, 0)) AS credit, " +
//            "a.id, a.title, a.code, a.hasSL, std.`type` " +
//            "FROM StockTransactionDetail std " +
//            "INNER JOIN StockTransaction st ON st.id = std.FK_stockTransactionId " +
//            "INNER JOIN ItemStock ii ON ii.id = std.FK_itemStockId " +
//            "INNER JOIN Item i ON i.id = ii.FK_itemId " +
//            "INNER JOIN Account a ON a.id = i.FK_assetAccountId " +
//            "INNER JOIN ItemTransactionDetail itd ON itd.FK_itemStockId = std.FK_itemStockId " +
//            "WHERE st.FK_transactionId = :transId) T1 GROUP BY id ORDER BY debit DESC, credit, code", nativeQuery = true)
//    List<Object[]> findGLAccountEntriesByTransactionIdMST(@Param("transId") Integer transId);

    @Query(value = "SELECT " +
            "SUM(debit), " +
            "SUM(credit), " +
            "id, " +
            "title, " +
            "code, " +
            "hasSL " +
            "FROM ( " +
            " " +
            "  SELECT  " +
            "  IF(type = 2, StockTransactionDetail.totalCost, 0) AS debit,  " +
            "  IF(type = 1, StockTransactionDetail.totalCost, 0) AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_debitAccountId  " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            "   " +
            "  UNION " +
            "   " +
            "  SELECT  " +
            "  IF(type = 2, 0, StockTransactionDetail.totalCost) AS debit,  " +
            "  IF(type = 1, 0, StockTransactionDetail.totalCost) AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_creditAccountId " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            " " +
            ") T1 GROUP BY id ORDER BY debit DESC, credit, code ", nativeQuery = true)
    List<Object[]> findGLAccountEntriesByTransactionIdMST(@Param("transId") Integer transId);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "Group BY Item.id " +
            "ORDER BY Item.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail " +
                    "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
                    "JOIN Item ON ItemStock.FK_itemId = Item.id " +
                    "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
                    "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
                    "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
                    "Group BY Item.id ",
            nativeQuery = true)
    Page<Object[]> findAllForSummaryByDateRangePaged(@Param("from") String from, @Param("to") String to, Pageable pageable);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
            "Group BY Item.id " +
            "ORDER BY Item.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail " +
                    "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
                    "JOIN Item ON ItemStock.FK_itemId = Item.id " +
                    "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
                    "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
                    "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
                    "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "Group BY Item.id ",
            nativeQuery = true)
    Page<Object[]> findAllForSummaryByDateRangeAndInventoryCategoryPaged(@Param("from") String from, @Param("to") String to, @Param("inventoryCategoryId") Integer inventoryCategoryId, Pageable pageable);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
            "Group BY Item.id " +
            "ORDER BY Item.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail " +
                    "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
                    "JOIN Item ON ItemStock.FK_itemId = Item.id " +
                    "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
                    "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
                    "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
                    "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
                    "Group BY Item.id ",
            nativeQuery = true)
    Page<Object[]> findAllForSummaryByDateRangeAndInventoryLocationPaged(@Param("from") String from, @Param("to") String to, @Param("inventoryLocationId") Integer inventoryLocationId, Pageable pageable);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
            "Group BY Item.id " +
            "ORDER BY Item.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail " +
                    "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
                    "JOIN Item ON ItemStock.FK_itemId = Item.id " +
                    "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
                    "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
                    "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
                    "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
                    "Group BY Item.id ",
            nativeQuery = true)
    Page<Object[]> findAllForSummaryByDateRangeAndInventoryCategoryAndInventoryLocationPaged(@Param("from") String from, @Param("to") String to, @Param("inventoryCategoryId") Integer inventoryCategoryId, @Param("inventoryLocationId") Integer inventoryLocationId, Pageable pageable);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "Group BY Item.id " +
            "ORDER BY Item.description ", nativeQuery = true)
    List<Object[]> findAllForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
            "Group BY Item.id " +
            "ORDER BY Item.description ", nativeQuery = true)
    List<Object[]> findAllForSummaryByDateRangeAndInventoryCategory(@Param("from") String from, @Param("to") String to, @Param("inventoryCategoryId") Integer inventoryCategoryId);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
            "Group BY Item.id " +
            "ORDER BY Item.description ", nativeQuery = true)
    List<Object[]> findAllForSummaryByDateRangeAndInventoryLocation(@Param("from") String from, @Param("to") String to, @Param("inventoryLocationId") Integer inventoryLocationId);

    @Query(value = "SELECT Item.id, " +
            "Item.code, " +
            "Item.description, " +
            "sum(StockTransactionDetail.quantity) as quantityReleased, " +
            "StockRelease.voucherDate, " +
            "Item.FK_inventoryCategoryId, " +
            "StockTransactionDetail.FK_inventoryLocationId " +
            "FROM StockTransactionDetail " +
            "JOIN ItemStock ON StockTransactionDetail.FK_itemStockId = ItemStock.id " +
            "JOIN Item ON ItemStock.FK_itemId = Item.id " +
            "JOIN StockTransaction ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "JOIN StockRelease ON StockRelease.FK_transactionId = StockTransaction.FK_transactionId " +
            "WHERE StockRelease.voucherDate BETWEEN :from AND :to " +
            "AND Item.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND StockTransactionDetail.FK_inventoryLocationId = :inventoryLocationId " +
            "Group BY Item.id " +
            "ORDER BY Item.description ", nativeQuery = true)
    List<Object[]> findAllForSummaryByDateRangeAndInventoryCategoryAndInventoryLocation(@Param("from") String from, @Param("to") String to, @Param("inventoryCategoryId") Integer inventoryCategoryId, @Param("inventoryLocationId") Integer inventoryLocationId);

    @Query(value = "SELECT stds.id, i.code, i.description, stds.quantity, i.id as itemId FROM StockTransactionDetail stds " +
            "JOIN StockTransaction st ON stds.FK_stockTransactionId = st.id " +
            "JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN ItemStock ist ON stds.FK_itemStockId = ist.id " +
            "INNER JOIN Item i ON ist.FK_itemId = i.id " +
            "WHERE i.FK_inventoryCategoryId = :inventoryLocationId " +
            "AND IF(LENGTH(:query) > 0, (i.code LIKE CONCAT('%', :query, '%') OR i.description LIKE CONCAT('%', :query, '%')), 1) " +
            "ORDER BY i.description \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockTransactionDetail stds " +
                    "JOIN StockTransaction st ON stds.FK_stockTransactionId = st.id " +
                    "JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
                    "INNER JOIN ItemStock ist ON stds.FK_itemStockId = ist.id " +
                    "INNER JOIN Item i ON ist.FK_itemId = i.id " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryLocationId " +
                    "AND IF(LENGTH(:query) > 0, (i.code LIKE CONCAT('%', :query, '%') OR i.description LIKE CONCAT('%', :query, '%')), 1) ",
            nativeQuery = true)
    Page<Object[]> findAllForSpecialEquipmentAssignment(@Param("query") String query, @Param("inventoryLocationId") Integer inventoryLocationId, Pageable paging);

    @Query(value = "SELECT  " +
            "stds.serialNo,  " +
            "sr.voucherDate,  " +
            "sr.code,  " +
            "u.fullName,  " +
            "sea.date,  " +
            "sead.FK_turnOnOrderId " +
            "FROM StockRelease sr " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
            "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
            "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
            "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE sr.voucherDate BETWEEN :from AND :to " +
            "AND se.FK_specialEquipmentTypeId = :typeId " +
            "ORDER BY sr.voucherDate, stds.serialNo  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockRelease sr " +
                    "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
                    "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
                    "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
                    "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
                    "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
                    "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
                    "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
                    "WHERE sr.voucherDate BETWEEN :from AND :to " +
                    "AND se.FK_specialEquipmentTypeId = :typeId ",
            nativeQuery = true)
    Page<Object[]> findAllForSpecialEquipmentReleaseSummaryByDateRangeAndSpecialEquipmentTypeIdPaged(@Param("from") String from, @Param("to") String to, @Param("typeId") Integer typeId, Pageable pageable);

    @Query(value = "SELECT  " +
            "stds.serialNo,  " +
            "sr.voucherDate,  " +
            "sr.code,  " +
            "u.fullName,  " +
            "sea.date,  " +
            "sead.FK_turnOnOrderId " +
            "FROM StockRelease sr " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
            "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
            "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
            "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE sr.voucherDate BETWEEN :from AND :to " +
            "ORDER BY sr.voucherDate, stds.serialNo  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockRelease sr " +
                    "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
                    "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
                    "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
                    "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
                    "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
                    "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
                    "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
                    "WHERE sr.voucherDate BETWEEN :from AND :to ",
            nativeQuery = true)
    Page<Object[]> findAllForSpecialEquipmentReleaseSummaryByDateRangePaged(@Param("from") String from, @Param("to") String to, Pageable pageable);

    @Query(value = "SELECT  " +
            "stds.serialNo,  " +
            "sr.voucherDate,  " +
            "sr.code,  " +
            "u.fullName,  " +
            "sea.date,  " +
            "sead.FK_turnOnOrderId " +
            "FROM StockRelease sr " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
            "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
            "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
            "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE sr.voucherDate BETWEEN :from AND :to " +
            "AND se.FK_specialEquipmentTypeId = :typeId " +
            "ORDER BY sr.voucherDate, stds.serialNo", nativeQuery = true)
    List<Object[]> findAllForSpecialEquipmentReleaseSummaryByDateRangeAndSpecialEquipmentTypeId(@Param("from") String from, @Param("to") String to, @Param("typeId") Integer typeId);

    @Query(value = "SELECT  " +
            "stds.serialNo,  " +
            "sr.voucherDate,  " +
            "sr.code,  " +
            "u.fullName,  " +
            "sea.date,  " +
            "sead.FK_turnOnOrderId " +
            "FROM StockRelease sr " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetail sttd ON sttd.FK_stockTransactionId = st.id " +
            "INNER JOIN StockTransactionDetailSerialNo stds ON stds.FK_stockTransactionDetailId = sttd.id " +
            "INNER JOIN SpecialEquipmentAssignment sea ON sea.FK_stockReleaseId = sr.id " +
            "INNER JOIN SpecialEquipmentAssignmentDetail sead ON sead.FK_specialEquipmentAssignmentId = sea.id " +
            "INNER JOIN SpecialEquipment se ON se.id = sead.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE sr.voucherDate BETWEEN :from AND :to " +
            "ORDER BY sr.voucherDate, stds.serialNo ", nativeQuery = true)
    List<Object[]> findAllForSpecialEquipmentReleaseSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "stdsn.serialNo, " +
            "sr.voucherDate, " +
            "sr.code, " +
            "u.fullName " +
            "FROM StockTransactionDetail std " +
            "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
            "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE se.FK_specialEquipmentTypeId = :typeId " +
            "AND se.id IN(" +
            "SELECT se.id FROM SpecialEquipment se " +
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
            "AND ist.id = std.FK_itemStockId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            ") " +
            "ORDER BY sr.voucherDate, stdsn.serialNo  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail std " +
                    "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
                    "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
                    "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
                    "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
                    "WHERE se.FK_specialEquipmentTypeId = :typeId " +
                    "AND se.id IN(" +
                    "SELECT se.id FROM SpecialEquipment se " +
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
                    "AND ist.id = std.FK_itemStockId " +
                    "AND cset.id IS NOT NULL " +
                    "OR mtd.id IS NOT NULL " +
                    "OR tt.id IS NOT NULL " +
                    ")",
            nativeQuery = true)
    Page<Object[]> findAllForSpecialEquipmentPendingSummaryBySpecialEquipmentTypeIdPaged(@Param("typeId") Integer typeId, Pageable pageable);

    @Query(value = "SELECT " +
            "stdsn.serialNo, " +
            "sr.voucherDate, " +
            "sr.code, " +
            "u.fullName " +
            "FROM StockTransactionDetail std " +
            "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
            "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE se.id IN(" +
            "SELECT se.id FROM SpecialEquipment se " +
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
            "AND ist.id = std.FK_itemStockId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            ") " +
            "ORDER BY sr.voucherDate, stdsn.serialNo  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM StockTransactionDetail std " +
                    "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
                    "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
                    "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
                    "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
                    "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
                    "WHERE se.id IN(" +
                    "SELECT se.id FROM SpecialEquipment se " +
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
                    "AND ist.id = std.FK_itemStockId " +
                    "AND cset.id IS NOT NULL " +
                    "OR mtd.id IS NOT NULL " +
                    "OR tt.id IS NOT NULL " +
                    ")",
            nativeQuery = true)
    Page<Object[]> findAllForSpecialEquipmentPendingSummaryPaged(Pageable pageable);

    @Query(value = "SELECT " +
            "stdsn.serialNo, " +
            "sr.voucherDate, " +
            "sr.code, " +
            "u.fullName " +
            "FROM StockTransactionDetail std " +
            "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
            "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE se.FK_specialEquipmentTypeId = :typeId " +
            "AND se.id IN(" +
            "SELECT se.id FROM SpecialEquipment se " +
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
            "AND ist.id = std.FK_itemStockId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            ") " +
            "ORDER BY sr.voucherDate, stdsn.serialNo", nativeQuery = true)
    List<Object[]> findAllForSpecialEquipmentPendingSummaryBySpecialEquipmentTypeId(@Param("typeId") Integer typeId);

    @Query(value = "SELECT " +
            "stdsn.serialNo, " +
            "sr.voucherDate, " +
            "sr.code, " +
            "u.fullName " +
            "FROM StockTransactionDetail std " +
            "INNER JOIN StockTransaction st ON std.FK_stockTransactionId = st.id " +
            "INNER JOIN StockRelease sr ON st.FK_transactionId = sr.FK_transactionId " +
            "INNER JOIN StockTransactionDetailSerialNo stdsn ON std.id = stdsn.FK_stockTransactionDetailId " +
            "INNER JOIN SpecialEquipment se ON se.id = stdsn.FK_specialEquipmentId " +
            "INNER JOIN User u ON u.id = sr.FK_receivedByUserId " +
            "WHERE se.id IN(" +
            "SELECT se.id FROM SpecialEquipment se " +
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
            "AND ist.id = std.FK_itemStockId " +
            "AND cset.id IS NOT NULL " +
            "OR mtd.id IS NOT NULL " +
            "OR tt.id IS NOT NULL " +
            ") " +
            "ORDER BY sr.voucherDate, stdsn.serialNo", nativeQuery = true)
    List<Object[]> findAllForSpecialEquipmentPendingSummary();

    @Query(value = "SELECT " +
            "SUM(debit), " +
            "SUM(credit), " +
            "id, " +
            "title, " +
            "code, " +
            "hasSL " +
            "FROM ( " +
            " " +
            "  SELECT  " +
            "  StockTransactionDetail.totalCost AS debit,  " +
            "  0 AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_debitAccountId  " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            "   " +
            "  UNION " +
            "   " +
            "  SELECT  " +
            "  0 AS debit,  " +
            "  StockTransactionDetail.totalCost AS credit,  " +
            "  Account.id, Account.title, Account.code, Account.hasSL, StockTransactionDetail.`type` " +
            "  FROM StockTransaction " +
            "  INNER JOIN StockTransactionDetail ON StockTransactionDetail.FK_stockTransactionId = StockTransaction.id " +
            "  INNER JOIN ItemStockDetail ON ItemStockDetail.id = StockTransactionDetail.FK_itemStockDetailId " +
            "  INNER JOIN Account ON Account.id = ItemStockDetail.FK_creditAccountId " +
            "  WHERE StockTransaction.FK_transactionId = :transId " +
            " " +
            ") T1 GROUP BY id ORDER BY debit DESC, credit, code ", nativeQuery = true)
    List<Object[]> findGLAccountSettingEntriesByTransactionIdForJV(@Param("transId") Integer transId);

}
