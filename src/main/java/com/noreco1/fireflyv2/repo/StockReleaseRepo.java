package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockRelease;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by lenovo on 5/4/2017.
 */
public interface StockReleaseRepo extends JpaRepository<StockRelease, Integer> {
    List<StockRelease> findByCode(String code);
    StockRelease findOneByTransactionId(Integer id);
    StockRelease findOneByDocumentTransactionId(Integer id);
    StockRelease findFirstByOrderByIdAsc();
    @Query("select s from StockWithdrawal s where s.documentStatus.id = ?1 and (s.code like ?2)")
    Page<StockRelease> findByStatusAndFilter(Integer documentStatusId, String filter, Pageable pageable);
    @Query(value = "SELECT e.code FROM StockRelease e WHERE year = :year AND type = :type ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year, @Param("type") Integer type);

    @Query(value = "select " +
            "la.* " +
            "from StockRelease la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` issueUser on la.FK_issuedByUserId = issueUser.id " +
            "LEFT JOIN `User` auditUser on la.FK_auditedByUserId = auditUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId OR receiveUser.id = :userId OR checkUser.id = :userId OR auditUser.id = :userId " +
            "OR issueUser.id = :userId)",
            nativeQuery = true)
    List<StockRelease> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                         @Param("from") Date from,
                                                                                         @Param("to") Date to,
                                                                                         @Param("documentStatusIds") Collection<Integer> documentStatusIds);
    @Query(value = "select " +
            "la.* " +
            "from StockRelease la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` issueUser on la.FK_issuedByUserId = issueUser.id " +
            "LEFT JOIN `User` auditUser on la.FK_auditedByUserId = auditUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR receiveUser.id = :userId OR checkUser.id = :userId OR auditUser.id = :userId " +
            "OR issueUser.id = :userId)",
            nativeQuery = true)
    List<StockRelease> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdAndOfficeId(@Param("userId") Integer userId,
                                                                                         @Param("from") Date from,
                                                                                         @Param("to") Date to,
                                                                                         @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "select * from StockWithdrawal WHERE StockWithdrawal.FK_documentStatusId = :documentStatusId",nativeQuery = true)
    List<StockWithdrawal> findStockWithdrawalByDocumentStatusId(Integer documentStatusId);

    @Query(value = "SELECT " +
            "InventoryLocation.description location, " +
            "ItemStockDetail.quantity " +
            "FROM ItemStockDetail " +
            "JOIN ItemStock ON ItemStock.id = ItemStockDetail.FK_itemStockId " +
            "JOIN InventoryLocation ON InventoryLocation.id = ItemStock.FK_InventoryLocationId " +
            "WHERE ItemStock.FK_itemId = :itemId AND ItemStock.FK_inventoryLocationId = :invLocId AND ItemStockDetail.FK_departmentId = :departmentId ", nativeQuery = true)
    List<Object[]> getAvailableItemStock(@Param("itemId") Integer itemId, @Param("invLocId") Integer invLocId, @Param("departmentId") Integer departmentId);

    @Query(value = "SELECT * FROM StockRelease " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
            "AND type = :typeId " +
            "AND FK_documentStatusId = 7 " +
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId,0) FROM AccountSetting) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
                    "AND type = :typeId " +
                    "AND FK_documentStatusId = 7 " +
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)" +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId,0) FROM AccountSetting) ",
            nativeQuery = true)
    Page<StockRelease> findByCodeContainingIgnoreCaseOrDescriptionContainingAndTypeIgnoreCaseOrderByCodeAsc(@Param("query") String query, @Param("typeId") Integer type, Pageable pageable);

    @Query(value = "SELECT * FROM StockRelease " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId,0) FROM AccountSetting) " +
            "AND type = :typeId " +
            "AND FK_documentStatusId = 7 " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId,0) FROM AccountSetting) " +
                    "AND type = :typeId " +
                    "AND FK_documentStatusId = 7",
            nativeQuery = true)
    Page<StockRelease> findByTransactionIdNotIn(@Param("typeId") Integer type, Pageable paging);

    Page<StockRelease> findByType(Integer type, Pageable pageable);

    @Query(value = "SELECT * FROM StockRelease " +
            "WHERE voucherDate BETWEEN :start AND :end " +
            "AND type = :type OR UPPER(code) LIKE :query " +
            "AND id NOT IN (SELECT FK_stockReleaseId FROM MaterialCreditTicket) " +
            "ORDER BY voucherDate, code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease " +
                    "WHERE voucherDate BETWEEN :start AND :end LIKE :query " +
                    "AND type = :type OR UPPER(code) LIKE :query " +
                    "AND id NOT IN (SELECT FK_stockReleaseId FROM MaterialCreditTicket)",
            nativeQuery = true)
    Page<StockRelease> findAllByVoucherDateBetweenAndCodeContainingIgnoreCaseAndTypeOrderByVoucherDateAscCodeAsc(@Param("start") Date start,
                                                                                                                 @Param("end") Date end,
                                                                                                                 @Param("query") String query,
                                                                                                                 @Param("type") Integer type, Pageable pageable);
    @Query(value = "SELECT * FROM StockRelease " +
            "WHERE voucherDate BETWEEN :start AND :end " +
            "AND type = :type " +
            "AND id NOT IN (SELECT FK_stockReleaseId FROM MaterialCreditTicket) " +
            "ORDER BY voucherDate, code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease " +
                    "WHERE voucherDate BETWEEN :start AND :end " +
                    "AND type = :type " +
                    "AND id NOT IN (SELECT FK_stockReleaseId FROM MaterialCreditTicket)",
            nativeQuery = true)
    Page<StockRelease> findAllByVoucherDateBetweenAndTypeOrderByVoucherDateAscCodeAsc(@Param("start") Date start,
                                                                                      @Param("end") Date end,
                                                                                      @Param("type") Integer type, Pageable pageable);

    List<StockRelease> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<StockRelease> findByVoucherDateBetweenOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to);
    List<StockRelease> findByVoucherDateBetweenAndInventoryLocationIdOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId);
    List<StockRelease> findByVoucherDateBetweenAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer type);
    List<StockRelease> findByVoucherDateBetweenAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer statusId);
    List<StockRelease> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer statusId);
    List<StockRelease> findByVoucherDateBetweenAndInventoryLocationIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer type);
    List<StockRelease> findByVoucherDateBetweenAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer statusId, Integer type);
    List<StockRelease> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndTypeOrderByTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer statusId, Integer type);

    @Query(value = "SELECT * FROM StockRelease " +
            "WHERE TYPE = 2 " + //OSSP
            "AND FK_documentStatusId = 7 " + // Approved
            "AND id not in (SELECT FK_stockReleaseId FROM MaintenanceRecordMaterialRelease) " +
            "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR description LIKE CONCAT('%', :query, '%')), 1) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease " +
                    "WHERE TYPE = 2 " + //OSSP
                    "AND FK_documentStatusId = 7 " + // Approved
                    "AND id not in (SELECT FK_stockReleaseId FROM MaintenanceRecordMaterialRelease) " +
                    "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR description LIKE CONCAT('%', :query, '%')), 1) ",
            nativeQuery = true)
    Page<StockRelease> findAllForMaintenanceRecord(@Param("query") String query, Pageable paging);

    @Query(value = "SELECT sr.* FROM StockRelease sr " +
            "LEFT JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "LEFT JOIN StockTransactionDetail strd ON st.id = strd.FK_stockTransactionId " +
            "WHERE sr.type = 3 " + //OFE
            "AND sr.FK_documentStatusId = 7 " + // Approved
            "AND strd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE FK_stockTransactionDetailId = strd.id) " +
            "AND IF(LENGTH(:query) > 0, (sr.code LIKE CONCAT('%', :query, '%') OR sr.description LIKE CONCAT('%', :query, '%')), 1) " +
            "GROUP BY sr.code " +
            "ORDER BY sr.code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockRelease sr " +
                    "LEFT JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
                    "LEFT JOIN StockTransactionDetail strd ON st.id = strd.FK_stockTransactionId " +
                    "WHERE sr.type = 3 " + //OFE
                    "AND sr.FK_documentStatusId = 7 " + // Approved
                    "AND strd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE FK_stockTransactionDetailId = strd.id) " +
                    "AND IF(LENGTH(:query) > 0, (sr.code LIKE CONCAT('%', :query, '%') OR sr.description LIKE CONCAT('%', :query, '%')), 1) " +
                    "GROUP BY sr.code ",
            nativeQuery = true)
    Page<StockRelease> findAllForMemorandumReceipt(@Param("query") String query, Pageable paging);

    @Query(value = "SELECT * FROM StockRelease sr " +
            "LEFT JOIN SpecialEquipmentAssignment sea ON sr.id = sea.FK_stockReleaseId " +
            "WHERE sr.FK_documentTransactionId IN (SELECT sw.FK_transactionId FROM StockWithdrawal sw " +
            "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased " +
            "FROM StockWithdrawalDetail swd " +
            "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
            "WHERE totalQuantity = totalReleased " +
            "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL) " +
            "AND sea.id is null " +
            "ORDER BY sr.voucherDate  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockRelease sr " +
                    "LEFT JOIN SpecialEquipmentAssignment sea ON sr.id = sea.FK_stockReleaseId " +
                    "WHERE sr.FK_documentTransactionId IN (SELECT sw.FK_transactionId FROM StockWithdrawal sw " +
                    "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased " +
                    "FROM StockWithdrawalDetail swd " +
                    "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
                    "WHERE totalQuantity = totalReleased " +
                    "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL) " +
                    "AND sea.id is null", nativeQuery = true)
    Page<StockRelease> findAllForSpecialEquipmentAssignment(@Param("inventoryCategoryId") Integer inventoryCategoryId, Pageable pageable);

    @Query(value = "SELECT * FROM StockRelease sr " +
            "LEFT JOIN SpecialEquipmentAssignment sea ON sr.id = sea.FK_stockReleaseId " +
            "WHERE sr.FK_documentTransactionId IN (SELECT sw.FK_transactionId FROM StockWithdrawal sw " +
            "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased " +
            "FROM StockWithdrawalDetail swd " +
            "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
            "WHERE totalQuantity = totalReleased " +
            "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL) " +
            "AND sea.id is null " +
            "AND sr.code LIKE :query " +
            "ORDER BY sr.voucherDate  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockRelease sr " +
                    "LEFT JOIN SpecialEquipmentAssignment sea ON sr.id = sea.FK_stockReleaseId " +
                    "WHERE sr.FK_documentTransactionId IN (SELECT sw.FK_transactionId FROM StockWithdrawal sw " +
                    "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased " +
                    "FROM StockWithdrawalDetail swd " +
                    "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
                    "WHERE totalQuantity = totalReleased " +
                    "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL) " +
                    "AND sea.id is null " +
                    "AND sr.code LIKE :query", nativeQuery = true)
    Page<StockRelease> findAllByQueryForSpecialEquipmentAssignment(@Param("query") String query,
                                                                   @Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                                   Pageable pageable);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM StockRelease   " +
            "WHERE StockRelease.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(StockRelease.code) LIKE :query  " +
            "AND StockRelease.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId, 0) FROM AccountSetting) GROUP BY StockRelease.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM StockRelease   " +
                    "WHERE StockRelease.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(StockRelease.code) LIKE :query  " +
                    "AND StockRelease.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId, 0) FROM AccountSetting) GROUP BY StockRelease.id ",
            nativeQuery = true)
    Page<StockRelease> findAllByQueryForAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM StockRelease " +
            "WHERE StockRelease.FK_documentStatusId = :documentStatusId " +
            "AND StockRelease.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId, 0) FROM AccountSetting) GROUP BY StockRelease.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM StockRelease " +
                    "WHERE StockRelease.FK_documentStatusId = :documentStatusId " +
                    "AND StockRelease.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReleaseId, 0) FROM AccountSetting) GROUP BY StockRelease.id",
            nativeQuery = true)
    Page<StockRelease> findAllForAccountSetting(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

}
