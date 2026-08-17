package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockReceive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface StockReceiveRepo extends JpaRepository<StockReceive, Integer> {
    List<StockReceive> findByCode(String code);
    StockReceive findOneByTransactionId(Integer id);
    StockReceive findFirstByOrderByIdAsc();
    @Query(value = "SELECT e.code FROM StockReceive e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);
    @Query(value = "select " +
            "la.* " +
            "from StockReceive la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId)",
            nativeQuery = true)
    List<StockReceive> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                              @Param("from") Date from,
                                                                                              @Param("to") Date to,
                                                                                              @Param("documentStatusId") Integer documentStatusId);
    @Query(value = "select " +
            "la.* " +
            "from StockReceive la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_officeId = :officeId AND FK_documentStatusId NOT IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId)",
            nativeQuery = true)
    List<StockReceive> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                              @Param("from") Date from,
                                                                                              @Param("to") Date to,
                                                                                              @Param("documentStatusIds") Collection<Integer> documentStatusIds);
    @Query(value = "select " +
            "la.* " +
            "from StockReceive la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_officeId = :officeId AND FK_documentStatusId NOT IN(:documentStatusIds)",
            nativeQuery = true)
    List<StockReceive> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("from") Date from,
                                                                                              @Param("to") Date to,
                                                                                              @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT * FROM StockReceive " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
            "AND FK_documentStatusId = 7 " +
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId,0) FROM AccountSetting) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockReceive " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(description) LIKE :query) " +
                    "AND FK_documentStatusId = 7 " +
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)" +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId,0) FROM AccountSetting) ",
            nativeQuery = true)
    Page<StockReceive> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCodeAsc(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM StockReceive " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId,0) FROM AccountSetting) " +
            "AND FK_documentStatusId = 7 " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM StockReceive " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId,0) FROM AccountSetting) " +
                    "AND FK_documentStatusId = 7",
            nativeQuery = true)
    Page<StockReceive> findByTransactionIdNotIn(Pageable paging);

    List<StockReceive> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    List<StockReceive> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(Date fromDate, Date toDate, Integer inventoryLocationId, Integer documentStatusId);

    List<StockReceive> findByVoucherDateBetweenAndInventoryLocationId(Date fromDate, Date toDate, Integer inventoryLocationId);

    List<StockReceive> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer documentStatusId);

    List<StockReceive> findByVoucherDateBetween(Date fromDate, Date toDate);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM StockReceive   " +
            "WHERE StockReceive.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(StockReceive.code) LIKE :query  " +
            "AND StockReceive.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM StockReceive   " +
                    "WHERE StockReceive.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(StockReceive.code) LIKE :query  " +
                    "AND StockReceive.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id ",
            nativeQuery = true)
    Page<StockReceive> findAllByQueryForAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM StockReceive " +
            "WHERE StockReceive.FK_documentStatusId = :documentStatusId " +
            "AND StockReceive.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM StockReceive " +
                    "WHERE StockReceive.FK_documentStatusId = :documentStatusId " +
                    "AND StockReceive.id NOT IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id",
            nativeQuery = true)
    Page<StockReceive> findAllForAccountSetting(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM StockReceive   " +
            "WHERE StockReceive.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(StockReceive.code) LIKE :query  " +
            "AND StockReceive.id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
            "AND StockReceive.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM StockReceive   " +
                    "WHERE StockReceive.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(StockReceive.code) LIKE :query  " +
                    "AND StockReceive.id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
                    "AND StockReceive.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) ",
            nativeQuery = true)
    Page<StockReceive> findAllByQueryForJV(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM StockReceive " +
            "WHERE StockReceive.FK_documentStatusId = :documentStatusId " +
            "AND StockReceive.id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
            "AND StockReceive.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM StockReceive " +
                    "WHERE StockReceive.FK_documentStatusId = :documentStatusId " +
                    "AND StockReceive.id IN (SELECT COALESCE(AccountSetting.FK_stockReceiveId, 0) FROM AccountSetting) GROUP BY StockReceive.id " +
                    "AND StockReceive.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) ",
            nativeQuery = true)
    Page<StockReceive> findAllForJV(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

}
