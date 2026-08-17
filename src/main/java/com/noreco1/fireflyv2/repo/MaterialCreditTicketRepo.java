package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialCreditTicket;
import com.noreco1.fireflyv2.model.StockReceive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface MaterialCreditTicketRepo extends JpaRepository<MaterialCreditTicket, Integer> {
    MaterialCreditTicket findByDocumentStatusId(Integer docId);

    Page<MaterialCreditTicket> findAll(Pageable paging);

    Page<MaterialCreditTicket> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM MaterialCreditTicket e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    MaterialCreditTicket findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM MaterialCreditTicket la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT " +
            "IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId " +
            "OR checkUser.id = :userId)",
            nativeQuery = true)
    List<MaterialCreditTicket> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                      @Param("from") Date from,
                                                                                                      @Param("to") Date to,
                                                                                                      @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM MaterialCreditTicket la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId " +
            "IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId " +
            "OR checkUser.id = :userId)",
            nativeQuery = true)
    List<MaterialCreditTicket> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                      @Param("from") Date from,
                                                                                                      @Param("to") Date to,
                                                                                                      @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT * FROM MaterialCreditTicket " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
            "AND FK_documentStatusId = 22 OR FK_documentStatusId = 7 " +  // Added Document Status ID 22 for Received Documents
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId,0) FROM AccountSetting) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM MaterialCreditTicket " +
                    "WHERE UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query " +
                    "AND FK_documentStatusId = 22 OR FK_documentStatusId = 7 " + // Added Document Status ID 22 for Received Documents
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)" +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId,0) FROM AccountSetting) ",
            nativeQuery = true)
    Page<MaterialCreditTicket> findByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseAndTransactionIdNotInOrderByCodeAsc(@Param("query") String query,
                                                                                                                              Pageable pageable);
    @Query(value = "SELECT * FROM MaterialCreditTicket " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId,0) FROM AccountSetting) " +
            "AND FK_documentStatusId = 22 OR FK_documentStatusId = 7 " + // Added Document Status ID 22 for Received Documents
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM MaterialCreditTicket " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId,0) FROM AccountSetting) " +
                    "AND FK_documentStatusId = 22 OR FK_documentStatusId = 7", // Added Document Status ID 22 for Received Documents
            nativeQuery = true)
    Page<MaterialCreditTicket> findByTransactionIdNotIn(Pageable paging);

    List<MaterialCreditTicket> findByVoucherDateBetweenAndStockReleaseInventoryLocationIdAndDocumentStatusId(Date fromDate, Date toDate, Integer location, Integer status);

    List<MaterialCreditTicket> findByVoucherDateBetweenAndStockReleaseInventoryLocationId(Date fromDate, Date toDate, Integer location);

    List<MaterialCreditTicket> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer status);

    List<MaterialCreditTicket> findByVoucherDateBetween(Date fromDate, Date toDate);

    List<MaterialCreditTicket> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM MaterialCreditTicket   " +
            "WHERE MaterialCreditTicket.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(MaterialCreditTicket.code) LIKE :query  " +
            "AND MaterialCreditTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId, 0) FROM AccountSetting) GROUP BY MaterialCreditTicket.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM MaterialCreditTicket   " +
                    "WHERE MaterialCreditTicket.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(MaterialCreditTicket.code) LIKE :query  " +
                    "AND MaterialCreditTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId, 0) FROM AccountSetting) GROUP BY MaterialCreditTicket.id ",
            nativeQuery = true)
    Page<MaterialCreditTicket> findAllByQueryForAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM MaterialCreditTicket " +
            "WHERE MaterialCreditTicket.FK_documentStatusId = :documentStatusId " +
            "AND MaterialCreditTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId, 0) FROM AccountSetting) GROUP BY MaterialCreditTicket.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM MaterialCreditTicket " +
                    "WHERE MaterialCreditTicket.FK_documentStatusId = :documentStatusId " +
                    "AND MaterialCreditTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialCreditTicketId, 0) FROM AccountSetting) GROUP BY MaterialCreditTicket.id",
            nativeQuery = true)
    Page<MaterialCreditTicket> findAllForAccountSetting(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

}
