package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucher;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

public interface CheckVoucherChequeRepo extends JpaRepository<CheckVoucherCheque, Integer> {
    public CheckVoucherCheque findOneByAccountIdAndTransactionId(Integer accountId, Integer transId);
    public CheckVoucherCheque findOneByBankAccountIdAndTransactionId(Integer bankAccountId, Integer transId);
    public Long deleteByTransactionId(Integer transId);
    public Long deleteByAccountIdAndTransactionId(Integer accountId, Integer transId);
    public Long deleteByBankAccountIdAndTransactionId(Integer bankAccountId, Integer transId);
    public List<CheckVoucherCheque> findByTransactionId(Integer transId);
    public List<CheckVoucherCheque> findByTransactionIdAndReleased(Integer transId, Boolean isReleased);

    @Query(value = "SELECT " +
            "cvc.id, " +
            "cvc.checkNumber, " +
            "SUM(gl.debit + gl.credit) AS amount, " +
            "cv.code, " +
            "cv.voucherDate, " +
            "cv.particulars, " +
            "a.title, " +
            "cvc.cleared, " +
            "cvc.FK_transactionId, " +
            "cvc.FK_accountId, " +
            "a.id as accountId, " +
            "sle.name as payee " +
            "FROM CheckVoucherCheque cvc " +
            "INNER JOIN GeneralLedger gl ON gl.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN CheckVoucher cv ON cv.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN SegmentAccount sa ON sa.id = gl.FK_segmentAccountId AND sa.FK_accountId = cvc.FK_accountId " +
            "INNER JOIN Account a ON a.id = sa.FK_accountId " +
            "LEFT JOIN slentity sle ON sle.accountNo = cv.FK_payeeAccountNo " +
            "WHERE cvc.released = :isReleased " +
            "GROUP BY cvc.id, cvc.checkNumber, cv.code, cv.voucherDate, cv.particulars, a.title, cvc.cleared, cvc.FK_transactionId, cvc.FK_accountId, a.id, sle.name", nativeQuery = true)
    public List<Object[]> findByReleasedWithCheckVoucherAndAmount(@Param("isReleased") Boolean isReleased);

    @Query(value = "SELECT code, voucherDate, checkNumber, SUM(GeneralLedger.credit) as checkAmount, `status`, released FROM CheckVoucherCheque " +
            "JOIN CheckVoucher ON CheckVoucherCheque.FK_transactionId = CheckVoucher.FK_transactionId " +
            "JOIN DocumentStatus ON CheckVoucher.FK_documentStatusId = DocumentStatus.id " +
            "JOIN GeneralLedger ON CheckVoucherCheque.FK_transactionId = GeneralLedger.FK_transactionId " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id  AND SegmentAccount.FK_accountId = CheckVoucherCheque.FK_accountId " +
            "WHERE voucherDate BETWEEN :from AND :to " +
            "GROUP BY code, CheckVoucherCheque.FK_accountId " +
            "ORDER BY code, checkNumber", nativeQuery = true)
    List<Object[]> findByVoucherDateRange(@Param("from") Date from, @Param("to") Date to);

    CheckVoucherCheque findFirstByBankAccountIdOrderByIdDesc(Integer bankAccountId);

    @Transactional
    @Query(value = "SELECT " +
            "cvrc.* " +
            "FROM CheckVoucherReleasedCheque cvrc " +
            "INNER JOIN CheckVoucherCheque cvc ON cvc.id = cvrc.FK_checkVoucherChequeId " +
            "INNER JOIN CheckVoucher cv ON cv.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN CheckVoucherApv cva ON cv.id = cva.FK_checkVoucherId " +
            "INNER JOIN AccountsPayableVoucher apv ON apv.id = cva.FK_accountsPayableVoucherId " +
            "INNER JOIN AccountsPayableVoucherLink apvl ON apvl.FK_accountsPayableVoucherId = apv.id " +
            "WHERE apvl.FK_documentTypeId = :documentTypeId AND apvl.FK_linkedDocumentId = :receivingReportId ", nativeQuery = true)
    List<Object[]> findAllByReceivingReportId(@Param("documentTypeId") Integer documentTypeId, @Param("receivingReportId") Integer receivingReportId);

}
