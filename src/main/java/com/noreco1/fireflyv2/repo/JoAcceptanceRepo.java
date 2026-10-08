package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JobOrderAcceptance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 7/7/2015.
 */
public interface JoAcceptanceRepo extends JpaRepository<JobOrderAcceptance, Integer> {
    public List<JobOrderAcceptance> findByCode(String code);
    @Query(value = "SELECT e.code FROM JobOrderAcceptance e WHERE year = :year  AND code LIKE '%JOA%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestJoAcceptanceCodeByYear(@Param("year") Integer year);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "FK_jobOrderId, " +
            "id " +
            "FROM JobOrderAcceptance " +
            "WHERE id = :joaId LIMIT 1", nativeQuery = true) // take one only for now
    public List<Object[]> findJobOrderById(@Param("joaId") Integer joaId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "* " +
            "FROM JobOrderAcceptance " +
            "WHERE hasPayReq = :hasPayReq " +
            "AND FK_documentStatusId = :statusId", nativeQuery = true)
    public List<JobOrderAcceptance> findByPayReqAndStatusId(@Param("hasPayReq") Boolean hasPayReq, @Param("statusId") Integer statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE JobOrderAcceptance SET hasPayReq = :hasPayReq WHERE id = :joaId", nativeQuery = true)
    public int updateHasPayReqById(@Param("hasPayReq") Boolean hasPayReq, @Param("joaId") Integer joaId);

    @Query(value = "SELECT " +
            "joa.id, " +
            "joa.code, " +
            "joa.voucherDate, " +
            "sup.name, " +
            "joa.amount, " +
            "joa.adjustment, " +
            "joa.netAmount, " +
            "d.status " +
            "FROM JobOrderAcceptance joa " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = joa.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = joa.FK_documentStatusId " +
            "WHERE joa.voucherDate >= :from AND joa.voucherDate <= :to " +
            "AND joa.FK_documentStatusId = :documentStatusId " +
            "ORDER BY joa.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "joa.id, " +
            "joa.code, " +
            "joa.voucherDate, " +
            "sup.name, " +
            "joa.amount, " +
            "joa.adjustment, " +
            "joa.netAmount, " +
            "d.status " +
            "FROM JobOrderAcceptance joa " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = joa.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = joa.FK_documentStatusId " +
            "WHERE joa.voucherDate >= :from AND joa.voucherDate <= :to " +
            "ORDER BY joa.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    public JobOrderAcceptance findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "doc.amount, " +
            "doc.adjustment, " +
            "doc.netAmount, " +
            "d.status " +
            "FROM JobOrderAcceptance doc " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId != :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    List<JobOrderAcceptance> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<JobOrderAcceptance> findByVoucherDateBetweenAndDocumentStatusIdNotInAndCreatedById(Date from, Date to, Collection<Integer> documentStatusIds, Integer createdByUserId);
    List<JobOrderAcceptance> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<JobOrderAcceptance> findByDocumentStatusIdAndVoucherDateBetweenAndCreatedById(Integer statusId, Date from, Date to, Integer createdByUserId);
    List<JobOrderAcceptance> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    List<JobOrderAcceptance> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    JobOrderAcceptance findFirstByOrderByIdAsc();

    List<JobOrderAcceptance> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<JobOrderAcceptance> findByDocumentStatusId(Integer status);

    @Query(value = "SELECT  " +
            "* " +
            "FROM JobOrderAcceptance  " +
            "JOIN slentity ON JobOrderAcceptance.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE JobOrderAcceptance.FK_documentStatusId = :documentStatusId " +
            "AND (UPPER(JobOrderAcceptance.code) LIKE :query OR UPPER(slentity.name) LIKE :query) " +
            "AND JobOrderAcceptance.id NOT IN ( " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ") GROUP BY JobOrderAcceptance.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM JobOrderAcceptance  " +
                    "JOIN slentity ON JobOrderAcceptance.FK_vendorAccountNo = slentity.accountNo " +
                    "WHERE JobOrderAcceptance.FK_documentStatusId = :documentStatusId " +
                    "AND (UPPER(JobOrderAcceptance.code) LIKE :query OR UPPER(slentity.name) LIKE :query) " +
                    "AND JobOrderAcceptance.id NOT IN ( " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ") GROUP BY JobOrderAcceptance.id ",
            nativeQuery = true)
    Page<JobOrderAcceptance> findAllByQueryForApv(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM JobOrderAcceptance  " +
            "WHERE JobOrderAcceptance.FK_documentStatusId = :documentStatusId " +
            "AND JobOrderAcceptance.id NOT IN ( " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ") GROUP BY JobOrderAcceptance.id " +
            "\n#pageable\n",
            countQuery = "SELECT " +
                    "COUNT(*) " +
                    "FROM JobOrderAcceptance " +
                    "WHERE JobOrderAcceptance.FK_documentStatusId = :documentStatusId " +
                    "AND JobOrderAcceptance.id NOT IN ( " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ") GROUP BY JobOrderAcceptance.id ",
            nativeQuery = true)
    Page<JobOrderAcceptance> findAllForApv(@Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, Pageable pageable);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "JobOrderAcceptance.id, " +
            "JobOrderAcceptance.code as code, " +
            "JobOrderAcceptance.netAmount, " +
            "CONCAT(JobOrderAcceptance.code, ' - ', slentity.name) as particulars, " +
            "JobOrderAcceptance.voucherDate, " +
            "u.fullName as preparedBy " +
            "FROM JobOrderAcceptance " +
            "JOIN User u ON JobOrderAcceptance.FK_createdByUserId = u.id  " +
            "JOIN slentity ON JobOrderAcceptance.FK_vendorAccountNo = slentity.accountNo " +
            "JOIN AccountsPayableVoucherLink ON JobOrderAcceptance.id = AccountsPayableVoucherLink.FK_linkedDocumentId " +
            "WHERE AccountsPayableVoucherLink.FK_accountsPayableVoucherId = :apvId " +
            "AND AccountsPayableVoucherLink.FK_documentTypeId = :docTypeId LIMIT 1", nativeQuery = true)
    public List<Object[]> findByApvId(@Param("apvId") Integer apvId, @Param("docTypeId") Integer docTypeId);

    @Query(value = "SELECT * FROM JobOrderAcceptance joa " +
            "WHERE joa.FK_documentStatusId = :documentStatusId " +
            "AND joa.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherJoAcceptance.FK_joAcceptanceId  " +
            "  FROM CheckVoucherJoAcceptance " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJoAcceptance.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM JobOrderAcceptance joa " +
                    "WHERE joa.FK_documentStatusId = :documentStatusId " +
                    "AND joa.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherJoAcceptance.FK_joAcceptanceId  " +
                    "  FROM CheckVoucherJoAcceptance " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJoAcceptance.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") ",
            nativeQuery = true)
    Page<JobOrderAcceptance> findAllByDocumentStatusForCv(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT * FROM JobOrderAcceptance joa " +
            "WHERE joa.FK_documentStatusId = :documentStatusId " +
            "AND joa.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherJoAcceptance.FK_joAcceptanceId  " +
            "  FROM CheckVoucherJoAcceptance " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJoAcceptance.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "AND joa.code LIKE :query " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM JobOrderAcceptance joa " +
                    "WHERE joa.FK_documentStatusId = :query " +
                    "AND joa.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherJoAcceptance.FK_joAcceptanceId  " +
                    "  FROM CheckVoucherJoAcceptance " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJoAcceptance.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") " +
                    "AND joa.code LIKE :query ",
            nativeQuery = true)
    Page<JobOrderAcceptance> findAllByQueryAndDocumentStatusForCv(@Param("query") String query,
                                                                  @Param("documentStatusId") Integer documentStatusId,
                                                                  Pageable pageable);

}
