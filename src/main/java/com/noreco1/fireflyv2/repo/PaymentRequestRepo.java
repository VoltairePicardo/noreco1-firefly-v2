package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 7/21/2015.
 */
public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, Integer> {
    public List<PaymentRequest> findOneByCode(String code);
    @Query(value = "SELECT " +
            "e.code " +
            "FROM PaymentRequest e " +
            "WHERE year = :year  AND code LIKE '%RFP%'ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestPaymentRequestCodeByYear(@Param("year") Integer year);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "FK_joAcceptanceId, " +
            "id " +
            "FROM PaymentRequest " +
            "WHERE id = :rpId LIMIT 1", nativeQuery = true) // take one only for now
    public List<Object[]> findJoAcceptanceById(@Param("rpId") Integer rpId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "DISTINCT PaymentRequest.id, " +
            "PaymentRequest.code as code, " +
            "JoAcceptance.netAmount, " +
            "CONCAT(JoAcceptance.code, ' - ', slentity.name) as particulars, " +
            "PaymentRequest.voucherDate, " +
            "u.fullName as preparedBy, " +
            "slentity.accountNo as accountNo, " +
            "slentity.name as slentityName " +
            "FROM PaymentRequest " +
            "JOIN JoAcceptance ON PaymentRequest.FK_joAcceptanceId = JoAcceptance.id " +
            "JOIN User u ON PaymentRequest.FK_createdByUserId = u.id  " +
            "JOIN slentity ON JoAcceptance.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE PaymentRequest.FK_documentStatusId = :documentStatusId " +
            "AND PaymentRequest.id NOT IN (SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "WHERE apv.FK_documentStatusId = :documentStatusId AND a.FK_documentTypeId = :docTypeId)", nativeQuery = true)
    public List<Object[]> findAllForApv(@Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "PaymentRequest.id, " +
            "PaymentRequest.code as code, " +
            "PaymentRequest.amount, " +
            "CONCAT(PaymentRequest.code, ' - ', slentity.name) as particulars, " +
            "PaymentRequest.voucherDate, " +
            "u.fullName as preparedBy " +
            "FROM PaymentRequest " +
            "JOIN User u ON PaymentRequest.FK_createdByUserId = u.id  " +
            "JOIN slentity ON PaymentRequest.FK_vendorAccountNo = slentity.accountNo " +
            "JOIN AccountsPayableVoucherLink ON PaymentRequest.id = AccountsPayableVoucherLink.FK_linkedDocumentId " +
            "WHERE AccountsPayableVoucherLink.FK_accountsPayableVoucherId = :apvId " +
            "AND AccountsPayableVoucherLink.FK_documentTypeId = :docTypeId LIMIT 1", nativeQuery = true)
    public List<Object[]> findByApvId(@Param("apvId") Integer apvId, @Param("docTypeId") Integer docTypeId);

    public PaymentRequest findOneByTransactionId(Integer id);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "joa.amount, " +
            "joa.adjustment, " +
            "joa.netAmount, " +
            "d.status " +
            "FROM PaymentRequest doc " +
            "INNER JOIN JoAcceptance joa on joa.id = doc.FK_joAcceptanceId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = joa.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId = :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "joa.amount, " +
            "joa.adjustment, " +
            "joa.netAmount, " +
            "d.status " +
            "FROM PaymentRequest doc " +
            "INNER JOIN JoAcceptance joa on joa.id = doc.FK_joAcceptanceId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = joa.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "joa.amount, " +
            "joa.adjustment, " +
            "joa.netAmount, " +
            "d.status " +
            "FROM PaymentRequest doc " +
            "INNER JOIN JoAcceptance joa on joa.id = doc.FK_joAcceptanceId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = joa.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId != :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);
    List<PaymentRequest> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<PaymentRequest> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<PaymentRequest> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    List<PaymentRequest> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    PaymentRequest findFirstByOrderByIdAsc();

    @Query(value = "SELECT  " +
            "* " +
            "FROM PaymentRequest  " +
            "JOIN slentity ON PaymentRequest.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE PaymentRequest.FK_documentStatusId = :documentStatusId " +
            "AND (UPPER(PaymentRequest.code) LIKE :query OR UPPER(slentity.name) LIKE :query) " +
            "AND PaymentRequest.id NOT IN ( " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ") GROUP BY PaymentRequest.id " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM PaymentRequest  " +
                    "JOIN slentity ON PaymentRequest.FK_vendorAccountNo = slentity.accountNo " +
                    "WHERE PaymentRequest.FK_documentStatusId = :documentStatusId " +
                    "AND (UPPER(PaymentRequest.code) LIKE :query OR UPPER(slentity.name) LIKE :query) " +
                    "AND PaymentRequest.id NOT IN ( " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ") GROUP BY PaymentRequest.id ",
            nativeQuery = true)
    Page<PaymentRequest> findAllByQueryForApv(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM PaymentRequest  " +
            "WHERE PaymentRequest.FK_documentStatusId = :documentStatusId " +
            "AND PaymentRequest.id NOT IN ( " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ") GROUP BY PaymentRequest.id " +
            "\n#pageable\n",
            countQuery = "SELECT " +
                    "COUNT(*) " +
                    "FROM PaymentRequest " +
                    "WHERE PaymentRequest.FK_documentStatusId = :documentStatusId " +
                    "AND PaymentRequest.id NOT IN ( " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ") GROUP BY PaymentRequest.id ",
            nativeQuery = true)
    Page<PaymentRequest> findAllForApv(@Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, Pageable pageable);
}
