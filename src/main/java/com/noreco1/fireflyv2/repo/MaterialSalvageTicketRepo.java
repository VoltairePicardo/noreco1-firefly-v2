package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialSalvageTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MaterialSalvageTicketRepo extends JpaRepository<MaterialSalvageTicket, Integer> {
//    MaterialSalvageTicket findByDocId(Integer docId);

    MaterialSalvageTicket findByDocumentStatusId(Integer docId);

    Page<MaterialSalvageTicket> findAll(Pageable paging);

    Page<MaterialSalvageTicket> findByCode(String code, Pageable paging);

    @Query(value = "SELECT " +
            " mst.id AS id, " +
            " mst.code AS code, " +
            " d.name as department, " +
            " mst.voucherDate AS voucherDate, " +
            " mst.purpose AS purpose, " +
            " u.fullname AS preparedBy, " +
            " ds.status AS status " +
            "FROM MaterialSalvageTicket mst " +
            "INNER JOIN User u ON u.id = mst.fk_createdbyuserid " +
            "INNER JOIN DocumentStatus ds ON ds.id = mst.fk_documentstatusid " +
            "INNER JOIN Department d on d.id = mst.FK_departmentId " +
            "WHERE mst.voucherDate >= :startDate " +
            "   AND mst.voucherDate <= :endDate " +
            "   AND (:statusId IS NULL OR mst.FK_documentStatusId = :statusId) " +
            "   AND (:query IS NULL OR :query = '' OR " +
            "       mst.code LIKE CONCAT('%', :query, '%') " +
            "       OR mst.purpose LIKE CONCAT('%', :query, '%') " +
            "       OR u.fullname LIKE CONCAT('%', :query, '%')) " +
            "   AND (mst.FK_createdByUserId = :userId " +
            "       OR mst.FK_returnedByUserId = :userId " +
            "       OR mst.FK_receivedByUserId = :userId) " +
            "ORDER BY mst.voucherDate DESC, mst.id DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM MaterialSalvageTicket mst " +
                    "INNER JOIN User u ON u.id = mst.fk_createdbyuserid " +
                    "WHERE mst.voucherDate >= :startDate " +
                    "   AND mst.voucherDate <= :endDate " +
                    "   AND (:statusId IS NULL OR mst.FK_documentStatusId = :statusId) " +
                    "   AND (:query IS NULL OR :query = '' OR " +
                    "       mst.code LIKE CONCAT('%', :query, '%') " +
                    "       OR mst.purpose LIKE CONCAT('%', :query, '%') " +
                    "       OR u.fullname LIKE CONCAT('%', :query, '%')) " +
                    "   AND (mst.FK_createdByUserId = :userId " +
                    "       OR mst.FK_returnedByUserId = :userId " +
                    "       OR mst.FK_receivedByUserId = :userId) ", nativeQuery = true)
    Page<Map<String, Object>> getMaterialSalvageTicketPaged(@Param("startDate") String startDate,
                                                              @Param("endDate") String endDate,
                                                              @Param("statusId") Integer statusId,
                                                              @Param("query") String query,
                                                              @Param("userId") Integer userId,
                                                              Pageable pageable);

    @Query(value = "SELECT e.code FROM MaterialSalvageTicket e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    MaterialSalvageTicket findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM MaterialSalvageTicket la  " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (receiveUser.id = :userId OR createUser.id = :userId)",
            nativeQuery = true)
    List<MaterialSalvageTicket> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                       @Param("from") Date from,
                                                                                                       @Param("to") Date to,
                                                                                                       @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM MaterialSalvageTicket la  " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId " +
            "IN(:documentStatusId) AND (receiveUser.id = :userId OR createUser.id = :userId)",
            nativeQuery = true)
    List<MaterialSalvageTicket> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                       @Param("from") Date from,
                                                                                                       @Param("to") Date to,
                                                                                                       @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT * FROM MaterialSalvageTicket " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(purpose) LIKE :query) " +
            "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
            "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId,0) FROM AccountSetting) " +
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM MaterialSalvageTicket " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(purpose) LIKE :query) " +
                    "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
                    "AND FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0)" +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId,0) FROM AccountSetting) ",
            nativeQuery = true)
    Page<MaterialSalvageTicket> findByCodeContainingIgnoreCaseOrPurposeContainingIgnoreCaseOrderByCodeAsc(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM MaterialSalvageTicket " +
            "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
            "AND id IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId,0) FROM AccountSetting) " +
            "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 " + // Added Document Status ID 22 for Received Documents
            "ORDER BY code \n#pageable\n",
            countQuery = "SELECT count(*) FROM MaterialSalvageTicket " +
                    "WHERE FK_transactionId NOT IN (SELECT invDocTransactionId FROM MaterialIssueRegister WHERE invDocTransactionId != 0) " +
                    "AND id IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId,0) FROM AccountSetting) " +
                    "AND FK_documentStatusId = 7 OR FK_documentStatusId = 22 ", // Added Document Status ID 22 for Received Documents
            nativeQuery = true)
    Page<MaterialSalvageTicket> findByTransactionIdNotIn(Pageable paging);

    List<MaterialSalvageTicket> findByVoucherDateBetweenAndInventoryLocationIdAndDocumentStatusId(Date fromDate, Date toDate, Integer location, Integer status);

    List<MaterialSalvageTicket> findByVoucherDateBetweenAndInventoryLocationId(Date fromDate, Date toDate, Integer location);

    List<MaterialSalvageTicket> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer status);

    List<MaterialSalvageTicket> findByVoucherDateBetween(Date fromDate, Date toDate);

    List<MaterialSalvageTicket> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM MaterialSalvageTicket   " +
            "WHERE MaterialSalvageTicket.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(MaterialSalvageTicket.code) LIKE :query  " +
            "AND MaterialSalvageTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId, 0) FROM AccountSetting) GROUP BY MaterialSalvageTicket.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM MaterialSalvageTicket   " +
                    "WHERE MaterialSalvageTicket.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(MaterialSalvageTicket.code) LIKE :query  " +
                    "AND MaterialSalvageTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId, 0) FROM AccountSetting) GROUP BY MaterialSalvageTicket.id ",
            nativeQuery = true)
    Page<MaterialSalvageTicket> findAllByQueryForAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM MaterialSalvageTicket " +
            "WHERE MaterialSalvageTicket.FK_documentStatusId = :documentStatusId " +
            "AND MaterialSalvageTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId, 0) FROM AccountSetting) GROUP BY MaterialSalvageTicket.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM MaterialSalvageTicket " +
                    "WHERE MaterialSalvageTicket.FK_documentStatusId = :documentStatusId " +
                    "AND MaterialSalvageTicket.id NOT IN (SELECT COALESCE(AccountSetting.FK_materialSalvageTicketId, 0) FROM AccountSetting) GROUP BY MaterialSalvageTicket.id",
            nativeQuery = true)
    Page<MaterialSalvageTicket> findAllForAccountSetting(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

}
