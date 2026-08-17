package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface StockAdjustmentRepo extends JpaRepository<StockAdjustment, Integer> {
    StockAdjustment findByDocumentStatusId(Integer docId);

    Page<StockAdjustment> findAll(Pageable paging);

    Page<StockAdjustment> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM StockAdjustment e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    StockAdjustment findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM StockAdjustment la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId)",
            nativeQuery = true)
    List<StockAdjustment> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM StockAdjustment la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId)",
            nativeQuery = true)
    List<StockAdjustment> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusId") Integer documentStatusId);
    @Query(value = "SELECT * FROM StockAdjustment " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
            "AND FK_documentStatusId = 7 " +
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId,0) FROM AccountSetting) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockAdjustment " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
                    "AND FK_documentStatusId = 7 " +
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)" +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId,0) FROM AccountSetting) ",
            nativeQuery = true)
    Page<StockAdjustment> findByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseOrderByCodeAsc(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM StockAdjustment " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId,0) FROM AccountSetting)" +
            "AND FK_documentStatusId = 7 " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockAdjustment " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId,0) FROM AccountSetting)" +
                    "AND FK_documentStatusId = 7",
            nativeQuery = true)
    Page<StockAdjustment> findByTransactionIdNotIn(Pageable paging);

    List<StockAdjustment> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    List<StockAdjustment> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(Date fromDate, Date toDate, Integer inventoryLocationId, Integer documentStatusId);

    List<StockAdjustment> findByVoucherDateBetweenAndInventoryLocationId(Date fromDate, Date toDate, Integer inventoryLocationId);

    List<StockAdjustment> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer documentStatusId);

    List<StockAdjustment> findByVoucherDateBetween(Date fromDate, Date toDate);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM StockAdjustment   " +
            "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(StockAdjustment.code) LIKE :query  " +
            "AND StockAdjustment.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM StockAdjustment   " +
                    "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(StockAdjustment.code) LIKE :query  " +
                    "AND StockAdjustment.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id ",
            nativeQuery = true)
    Page<StockAdjustment> findAllByQueryForAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM StockAdjustment " +
            "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId " +
            "AND StockAdjustment.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM StockAdjustment " +
                    "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId " +
                    "AND StockAdjustment.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id",
            nativeQuery = true)
    Page<StockAdjustment> findAllForAccountSetting(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM StockAdjustment   " +
            "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(StockAdjustment.code) LIKE :query  " +
            "AND StockAdjustment.id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
            "AND StockAdjustment.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM StockAdjustment   " +
                    "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(StockAdjustment.code) LIKE :query  " +
                    "AND StockAdjustment.id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
                    "AND StockAdjustment.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) ",
            nativeQuery = true)
    Page<StockAdjustment> findAllByQueryForJV(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM StockAdjustment " +
            "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId " +
            "AND StockAdjustment.id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
            "AND StockAdjustment.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM StockAdjustment " +
                    "WHERE StockAdjustment.FK_documentStatusId = :documentStatusId " +
                    "AND StockAdjustment.id IN (SELECT COALESCE(AccountSetting.FK_stockAdjustmentId, 0) FROM AccountSetting) GROUP BY StockAdjustment.id " +
                    "AND StockAdjustment.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) ",
            nativeQuery = true)
    Page<StockAdjustment> findAllForJV(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);
}
