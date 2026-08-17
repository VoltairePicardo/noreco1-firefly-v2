package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemsForRepair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tonyc on 9/22/2020.
 */
public interface ItemsForRepairRepo extends JpaRepository<ItemsForRepair, Integer> {
    ItemsForRepair findByDocumentStatusId(Integer docId);

    Page<ItemsForRepair> findAll(Pageable paging);

    Page<ItemsForRepair> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM ItemsForRepair e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    ItemsForRepair findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM ItemsForRepair la  " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (createUser.id = :userId)",
            nativeQuery = true)
    List<ItemsForRepair> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                       @Param("from") Date from,
                                                                                                       @Param("to") Date to,
                                                                                                       @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM ItemsForRepair la  " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId " +
            "IN(:documentStatusId) AND (createUser.id = :userId)",
            nativeQuery = true)
    List<ItemsForRepair> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                       @Param("from") Date from,
                                                                                                       @Param("to") Date to,
                                                                                                       @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT * FROM ItemsForRepair " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(purpose) LIKE :query) " +
            "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM ItemsForRepair " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(purpose) LIKE :query) " +
                    "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)",
            nativeQuery = true)
    Page<ItemsForRepair> findByCodeContainingIgnoreCaseOrPurposeContainingIgnoreCaseOrderByCodeAsc(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM ItemsForRepair " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM ItemsForRepair " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 ", // Added Document Status ID 22 for Received Documents
            nativeQuery = true)
    Page<ItemsForRepair> findByTransactionIdNotIn(Pageable paging);

    List<ItemsForRepair> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(Date fromDate, Date toDate, Integer location, Integer status);

    List<ItemsForRepair> findByVoucherDateBetweenAndInventoryLocationId(Date fromDate, Date toDate, Integer location);

    List<ItemsForRepair> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer status);

    List<ItemsForRepair> findByVoucherDateBetween(Date fromDate, Date toDate);

    List<ItemsForRepair> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "            ifr.id, ifr.code, ifr.voucherDate, ifr.particulars, u.fullName, ifr.FK_transactionId, s.FK_accountNo, s.name " +
            "            FROM ItemsForRepair ifr " +
            "            INNER JOIN ItemTransactionDetail itd ON ifr.FK_transactionId = itd.FK_transactionId " +
            "            INNER JOIN User u ON ifr.FK_createdByUserId = u.id " +
            "            LEFT JOIN Supplier s ON ifr.FK_supplierId = s.id " +
            "            WHERE itd.deductFromStock != 0 AND ifr.FK_documentStatusId = 20 " +
            "            AND ifr.FK_inventoryLocationId = :invLocId " +
            "            GROUP BY ifr.id " +
            "            HAVING SUM(itd.quantity) > SUM(itd.deliveredQuantity) " +
            "            ORDER BY ifr.id \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "            FROM ItemsForRepair ifr " +
                    "            INNER JOIN ItemTransactionDetail itd ON ifr.FK_transactionId = itd.FK_transactionId " +
                    "            INNER JOIN User u ON ifr.FK_createdByUserId = u.id " +
                    "            LEFT JOIN Supplier s ON ifr.FK_supplierId = s.id " +
                    "            WHERE itd.deductFromStock != 0 AND ifr.FK_documentStatusId = 20 " +
                    "            AND ifr.FK_inventoryLocationId = :invLocId " +
                    "            GROUP BY ifr.id " +
                    "            HAVING SUM(itd.quantity) > SUM(itd.deliveredQuantity) " +
                    "            ORDER BY ifr.id",
            nativeQuery = true)
    Page<Object[]> findForRR(@Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT " +
            "            ifr.id, ifr.code, ifr.voucherDate, ifr.particulars, u.fullName, ifr.FK_transactionId, s.FK_accountNo, s.name " +
            "            FROM ItemsForRepair ifr " +
            "            INNER JOIN ItemTransactionDetail itd ON ifr.FK_transactionId = itd.FK_transactionId " +
            "            INNER JOIN User u ON ifr.FK_createdByUserId = u.id " +
            "            LEFT JOIN Supplier s ON ifr.FK_supplierId = s.id " +
            "            WHERE itd.deductFromStock != 0 AND ifr.FK_documentStatusId = 20 " +
            "            AND ifr.FK_inventoryLocationId = :invLocId " +
            "AND (ifr.code LIKE :query OR ifr.particulars LIKE :query OR u.fullName LIKE :query) " +
            "            GROUP BY ifr.id " +
            "            HAVING SUM(itd.quantity) > SUM(itd.deliveredQuantity) " +
            "            ORDER BY ifr.id \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "            FROM ItemsForRepair ifr " +
                    "            INNER JOIN ItemTransactionDetail itd ON ifr.FK_transactionId = itd.FK_transactionId " +
                    "            INNER JOIN User u ON ifr.FK_createdByUserId = u.id " +
                    "            LEFT JOIN Supplier s ON ifr.FK_supplierId = s.id " +
                    "            WHERE itd.deductFromStock != 0 AND ifr.FK_documentStatusId = 20 " +
                    "            AND ifr.FK_inventoryLocationId = :invLocId " +
                    "AND (ifr.code LIKE :query OR ifr.particulars LIKE :query OR u.fullName LIKE :query) " +
                    "            GROUP BY ifr.id " +
                    "            HAVING SUM(itd.quantity) > SUM(itd.deliveredQuantity) " +
                    "            ORDER BY ifr.id",
            nativeQuery = true)
    Page<Object[]> findForRR(@Param("query") String query, @Param("invLocId") Integer invLocId,
                             Pageable pageable);
}
