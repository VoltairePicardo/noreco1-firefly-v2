package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ReleasedCheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReleasedCheckRepo extends JpaRepository<ReleasedCheque, Integer> {

    @Query(value = "select * from CheckVoucherReleasedCheque where CheckVoucherReleasedCheque.FK_checkVoucherChequeId = :checkVoucherChequeId ", nativeQuery = true)
    ReleasedCheque findByCheckVoucherChequeId(@Param("checkVoucherChequeId") Integer checkVoucherChequeId);

    @Query(value = "SELECT cvrc.id, cv.code, cv.voucherDate, cvc.checkNumber, " +
            "SUM(gl.credit) as checkAmount, " +
            "cvrc.dateReleased, cvrc.receivedBy, " +
            "CASE WHEN cvc.released = 1 THEN 'Released' ELSE 'Cancelled' END AS status, " +
            "cvrc.remarks, cvrc.orNumber, cvrc.idNumber, cvrc.depositSlip, " +
            "cvrc.personImage, cvrc.FK_transactionId as transactionId, " +
            "a.title as accountTitle, " +
            "sle.name as payee " +
            "FROM CheckVoucherReleasedCheque cvrc " +
            "INNER JOIN CheckVoucherCheque cvc ON cvc.id = cvrc.FK_checkVoucherChequeId " +
            "INNER JOIN CheckVoucher cv ON cv.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN GeneralLedger gl ON gl.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN SegmentAccount sa ON sa.id = gl.FK_segmentAccountId AND sa.FK_accountId = cvc.FK_accountId " +
            "INNER JOIN Account a ON a.id = sa.FK_accountId " +
            "LEFT JOIN slentity sle ON sle.accountNo = cv.FK_payeeAccountNo " +
            "WHERE cvrc.id = :id " +
            "GROUP BY cvrc.id, cv.code, cv.voucherDate, cvc.checkNumber, cvrc.dateReleased, cvrc.receivedBy, " +
            "cvc.released, cvrc.remarks, cvrc.orNumber, cvrc.idNumber, cvrc.depositSlip, cvrc.personImage, " +
            "cvrc.FK_transactionId, a.title, sle.name", nativeQuery = true)
    Object[] findDetailById(@Param("id") Integer id);

    @Query(value = "SELECT cvrc.id, cv.code, cv.voucherDate, cvc.checkNumber, " +
            "SUM(gl.credit) as checkAmount, " +
            "cvrc.dateReleased, cvrc.receivedBy, " +
            "CASE WHEN cvc.released = 1 THEN 'Released' ELSE 'Cancelled' END AS status, " +
            "cvrc.remarks, cvrc.orNumber, " +
            "u.fullName as releasedBy, " +
            "sle.name as releasedTo " +
            "FROM CheckVoucherReleasedCheque cvrc " +
            "INNER JOIN CheckVoucherCheque cvc ON cvc.id = cvrc.FK_checkVoucherChequeId " +
            "INNER JOIN CheckVoucher cv ON cv.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN GeneralLedger gl ON gl.FK_transactionId = cvc.FK_transactionId " +
            "INNER JOIN SegmentAccount sa ON sa.id = gl.FK_segmentAccountId AND sa.FK_accountId = cvc.FK_accountId " +
            "LEFT JOIN User u ON u.id = cvrc.FK_createdByUserId " +
            "LEFT JOIN slentity sle ON sle.accountNo = cv.FK_payeeAccountNo " +
            "WHERE (:from IS NULL OR cvrc.dateReleased >= :from) AND (:to IS NULL OR cvrc.dateReleased <= :to) " +
            "GROUP BY cvrc.id, cv.code, cv.voucherDate, cvc.checkNumber, cvrc.dateReleased, cvrc.receivedBy, " +
            "cvc.released, cvrc.remarks, cvrc.orNumber, u.fullName, sle.name " +
            "ORDER BY cvrc.dateReleased DESC", nativeQuery = true)
    List<Object[]> findReleasedWithDateRange(@Param("from") String from, @Param("to") String to);

}
