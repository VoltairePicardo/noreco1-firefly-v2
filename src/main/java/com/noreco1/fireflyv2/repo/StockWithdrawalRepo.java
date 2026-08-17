package com.noreco1.fireflyv2.repo;

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
public interface StockWithdrawalRepo extends JpaRepository<StockWithdrawal, Integer> {

    public List<StockWithdrawal> findByCode(String code);
    public StockWithdrawal findOneByPurchaseRequestId(Integer prId);
    public StockWithdrawal findOneByTransactionId(Integer id);
    StockWithdrawal findFirstByOrderByIdAsc();
    @Query("select s from StockWithdrawal s where s.documentStatus.id = ?1 and (s.code like ?2)")
    Page<StockWithdrawal> findByStatusAndFilter(Integer documentStatusId, String filter, Pageable pageable);
    @Query(value = "SELECT e.code FROM StockWithdrawal e WHERE year = :year AND type = :type ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year, @Param("type") Integer type);

    @Query(value = "select " +
            "la.* " +
            "from StockWithdrawal la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` issueUser on la.FK_issuedByUserId = issueUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId OR receiveUser.id = :userId OR checkUser.id = :userId " +
            "OR issueUser.id = :userId)",
            nativeQuery = true)
    List<StockWithdrawal> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusIds") Collection<Integer> documentStatusIds);
    @Query(value = "select " +
            "la.* " +
            "from StockWithdrawal la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` issueUser on la.FK_issuedByUserId = issueUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR receiveUser.id = :userId OR checkUser.id = :userId " +
            "OR issueUser.id = :userId)",
            nativeQuery = true)
    List<StockWithdrawal> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT * FROM StockWithdrawal " +
            "WHERE FK_documentStatusId = :documentStatus " +
            "AND id IN (SELECT FK_stockWithdrawalId " +
            "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
            "AND FK_inventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockWithdrawal " +
                    "WHERE FK_documentStatusId = :documentStatus " +
                    "AND id IN (SELECT FK_stockWithdrawalId " +
                    "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
                    "AND FK_inventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<StockWithdrawal> findAllByDocumentStatusAndIdNotIn(@Param("documentStatus") Integer documentStatus,
                                                            @Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT * FROM StockWithdrawal " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
            "AND FK_documentStatusId = :documentStatus " +
            "AND id IN (SELECT FK_stockWithdrawalId " +
            "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
            "AND FK_inventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockWithdrawal " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
                    "AND FK_documentStatusId = :documentStatus " +
                    "AND id IN (SELECT FK_stockWithdrawalId " +
                    "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
                    "AND FK_inventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<StockWithdrawal> findAllByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndDocumentStatusAndIdNotIn(@Param("query") String query,
                                                                                                                        @Param("documentStatus") Integer id,
                                                                                                                        @Param("invLocId") Integer invLocId, Pageable pageable);

    List<StockWithdrawal> findByVoucherDateBetweenOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to);
    List<StockWithdrawal> findByVoucherDateBetweenAndInventoryLocationIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId);
    List<StockWithdrawal> findByVoucherDateBetweenAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer type);
    List<StockWithdrawal> findByVoucherDateBetweenAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer statusId);
    List<StockWithdrawal> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer statusId);
    List<StockWithdrawal> findByVoucherDateBetweenAndInventoryLocationIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer type);
    List<StockWithdrawal> findByVoucherDateBetweenAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer statusId, Integer type);
    List<StockWithdrawal> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusIdAndInventoryCategoryTypeOrderByInventoryCategoryTypeAscInventoryLocationIdAscCodeAsc(Date from, Date to, Integer locationId, Integer statusId, Integer type);

    List<StockWithdrawal> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "SELECT * FROM StockWithdrawal sw " +
            "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased FROM StockWithdrawalDetail swd " +
            "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
            "LEFT JOIN SpecialEquipmentAssignment sea ON sw.id = sea.FK_stockWithdrawalId " +
            "WHERE totalQuantity = totalReleased " +
            "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL " +
            "AND sea.id is null " +
            "ORDER BY sw.voucherDate  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockWithdrawal sw " +
                    "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased FROM StockWithdrawalDetail swd " +
                    "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
                    "LEFT JOIN SpecialEquipmentAssignment sea ON sw.id = sea.FK_stockWithdrawalId " +
                    "WHERE totalQuantity = totalReleased " +
                    "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL " +
                    "AND sea.id is null", nativeQuery = true)
    Page<StockWithdrawal> findAllForSpecialEquipmentAssignment(@Param("inventoryCategoryId") Integer inventoryCategoryId, Pageable pageable);

    @Query(value = "SELECT * FROM StockWithdrawal sw " +
            "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased FROM StockWithdrawalDetail swd " +
            "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
            "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
            "LEFT JOIN SpecialEquipmentAssignment sea ON sw.id = sea.FK_stockWithdrawalId " +
            "WHERE totalQuantity = totalReleased " +
            "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL " +
            "AND sea.id is null " +
            "AND sw.code LIKE :query " +
            "ORDER BY sw.voucherDate  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockWithdrawal sw " +
                    "JOIN (SELECT swd.FK_stockWithdrawalId, SUM(swd.quantity) AS totalQuantity, SUM(swd.quantityReleased) AS totalReleased FROM StockWithdrawalDetail swd " +
                    "LEFT JOIN Item i ON swd.FK_itemId = i.id " +
                    "WHERE i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "GROUP BY swd.FK_stockWithdrawalId) AS detail ON detail.FK_stockWithdrawalId = sw.id " +
                    "LEFT JOIN SpecialEquipmentAssignment sea ON sw.id = sea.FK_stockWithdrawalId " +
                    "WHERE totalQuantity = totalReleased " +
                    "AND sw.FK_turnOnOrderWithdrawalId IS NOT NULL " +
                    "AND sea.id is null " +
                    "AND sw.code LIKE :query ", nativeQuery = true)
    Page<StockWithdrawal> findAllForSpecialEquipmentAssignmentByQuery(@Param("query") String query,
                                                                      @Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                                      Pageable pageable);

    @Query(value = "SELECT sw.* FROM StockWithdrawal sw " +
            "LEFT JOIN StockWithdrawalDetail swd ON sw.id = swd.FK_stockWithdrawalId " +
            "LEFT JOIN StockWithdrawalEmployee swe ON sw.id = swe.FK_stockWithdrawalId  " +
            "WHERE sw.type = 3 " + //OFE
            "AND sw.FK_documentStatusId = 7 " +
            "AND sw.FK_transactionId IN (SELECT sr.FK_documentTransactionId FROM StockRelease sr WHERE sr.FK_documentStatusId = 7) " + // Approved
            "AND swd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE MemorandumReceiptDetail.FK_stockWithdrawalDetailId = swd.id) " +
            "AND IF(LENGTH(:query) > 0, (sw.code LIKE CONCAT('%', :query, '%') OR sw.description LIKE CONCAT('%', :query, '%')), 1) " +
            "AND swe.id IS NULL " +
            "GROUP BY sw.code " +
            "ORDER BY sw.code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockWithdrawal sw " +
                    "LEFT JOIN StockWithdrawalDetail swd ON sw.id = swd.FK_stockWithdrawalId " +
                    "LEFT JOIN StockWithdrawalEmployee swe ON sw.id = swe.FK_stockWithdrawalId  " +
                    "WHERE sw.type = 3 " + //OFE
                    "AND sw.FK_documentStatusId = 7 " +
                    "AND sw.FK_transactionId IN (SELECT sr.FK_documentTransactionId FROM StockRelease sr WHERE sr.FK_documentStatusId = 7) " + // Approved
                    "AND swd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE MemorandumReceiptDetail.FK_stockWithdrawalDetailId = swd.id) " +
                    "AND IF(LENGTH(:query) > 0, (sw.code LIKE CONCAT('%', :query, '%') OR sw.description LIKE CONCAT('%', :query, '%')), 1) " +
                    "AND swe.id IS NULL " +
                    "GROUP BY sw.code " +
                    "ORDER BY sw.code ",
            nativeQuery = true)
    Page<StockWithdrawal> findAllForMemorandumReceipt(@Param("query") String query, Pageable paging);

    @Query(value = "SELECT sw.* FROM StockWithdrawal sw " +
            "INNER JOIN StockWithdrawalDetail swd ON sw.id = swd.FK_stockWithdrawalId " +
            "INNER JOIN StockWithdrawalEmployee swe ON sw.id = swe.FK_stockWithdrawalId " +
            "WHERE sw.type = 3 " + //OFE
            "AND sw.FK_documentStatusId = 7 " +
            "AND sw.FK_transactionId IN (SELECT sr.FK_documentTransactionId FROM StockRelease sr WHERE sr.FK_documentStatusId = 7) " + // Approved
            "AND swd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE MemorandumReceiptDetail.FK_stockWithdrawalDetailId = swd.id) " +
            "AND IF(LENGTH(:query) > 0, (sw.code LIKE CONCAT('%', :query, '%') OR sw.description LIKE CONCAT('%', :query, '%')), 1) " +
            "GROUP BY sw.code " +
            "ORDER BY sw.code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockWithdrawal sw " +
                    "INNER JOIN StockWithdrawalDetail swd ON sw.id = swd.FK_stockWithdrawalId " +
                    "INNER JOIN StockWithdrawalEmployee swe ON sw.id = swe.FK_stockWithdrawalId " +
                    "WHERE sw.type = 3 " + //OFE
                    "AND sw.FK_documentStatusId = 7 " +
                    "AND sw.FK_transactionId IN (SELECT sr.FK_documentTransactionId FROM StockRelease sr WHERE sr.FK_documentStatusId = 7) " + // Approved
                    "AND swd.quantity > (SELECT IF(sum(quantity) IS NOT NULL, sum(quantity), 0) FROM MemorandumReceiptDetail WHERE MemorandumReceiptDetail.FK_stockWithdrawalDetailId = swd.id) " +
                    "AND IF(LENGTH(:query) > 0, (sw.code LIKE CONCAT('%', :query, '%') OR sw.description LIKE CONCAT('%', :query, '%')), 1) " +
                    "GROUP BY sw.code " +
                    "ORDER BY sw.code ",
            nativeQuery = true)
    Page<StockWithdrawal> findAllForMemorandumReceiptMultipleEmployees(@Param("query") String query, Pageable paging);
}
