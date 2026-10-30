package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherIncomePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckVoucherIncomePaymentRepo extends JpaRepository<CheckVoucherIncomePayment, Integer> {
    Long deleteByTransactionId(Integer transId);
    List<CheckVoucherIncomePayment> findByTransactionId(Integer transId);
    CheckVoucherIncomePayment findOneByTransactionId(Integer transId);
    List<CheckVoucherIncomePayment> findByTransactionIdAndAccountId(Integer transId, Integer accountId);
    CheckVoucherIncomePayment findOneByTransactionIdAndTaxCodeIdAndAccountId(Integer transId, Integer taxCodeId, Integer accountId);

    @Query(value = "SELECT " +
            "TaxCode.description as natureOfIncomePayment, " +
            "TaxCode.code as atc, " +
            "CheckVoucherIncomePayment.percentage as taxRate, " +
            "SUM(CheckVoucherIncomePayment.baseAmount) as taxBase, " +
            "SUM(CheckVoucherIncomePayment.amount) as taxRequired " +
            "FROM CheckVoucher " +
            "JOIN CheckVoucherIncomePayment  ON CheckVoucherIncomePayment.FK_transactionId = CheckVoucher.FK_transactionId " +
            "JOIN TaxCode ON CheckVoucherIncomePayment.FK_taxCodeId = TaxCode.id " +
            "WHERE YEAR(voucherDate) = :year AND MONTH(voucherDate) = :month " +
            "AND CheckVoucher.FK_documentStatusId = :docStatId " +
            "GROUP BY TaxCode.code ORDER BY  TaxCode.code", nativeQuery = true)
    List<Object[]> getComputationOfTax(@Param("year") Integer year, @Param("month")Integer month, @Param("docStatId")Integer docStatId);

    @Query(value = "SELECT " +
            "Supplier.tin, " +
            "IF(TaxCode.payeeType = 'CORPORATE', Supplier.name, '') as corporate, " +
            "IF(TaxCode.payeeType = 'INDIVIDUAL', Supplier.name, '') as individual, " +
            "TaxCode.code as atc, " +
            "TaxCode.description as natureOfPayment, " +
            "CheckVoucherIncomePayment.baseAmount as amount, " +
            "CheckVoucherIncomePayment.percentage as taxRate, " +
            "CheckVoucherIncomePayment.amount as taxWithheld " +
            "FROM CheckVoucherIncomePayment " +
            "JOIN CheckVoucher ON CheckVoucherIncomePayment.FK_transactionId = CheckVoucher.FK_transactionId " +
            "JOIN TaxCode ON CheckVoucherIncomePayment.FK_taxCodeId = TaxCode.id " +
            "JOIN Supplier ON CheckVoucher.FK_payeeAccountNo = Supplier.FK_accountNo " +
            "WHERE CheckVoucher.FK_documentStatusId = :docStatId  " +
            "AND YEAR(voucherDate) = :year AND MONTH(voucherDate) = :month " +
            "ORDER BY Supplier.name", nativeQuery = true)
    List<Object[]> getComputationOfTaxSchedule(@Param("year") Integer year, @Param("month")Integer month, @Param("docStatId")Integer docStatId);



    @Query(value = "SELECT " +
            "   tc.description, " +
            "   tc.code, " +
            "   SUM(cvip.baseamount) AS baseAmount, " +
            "   cvip.percentage / 100 AS taxRate, " +
            "   SUM(cvip.amount) AS taxDue " +
            "FROM CheckVoucherIncomePayment cvip " +
            "INNER JOIN TaxCode tc ON tc.id = cvip.FK_taxCodeId " +
            "LEFT JOIN CheckVoucher cv ON cv.fk_transactionid = cvip.fk_transactionid " +
            "LEFT JOIN AccountsPayableVoucher apv ON apv.fk_transactionid = cvip.fk_transactionid " +
            "WHERE COALESCE(cv.voucherdate, apv.voucherdate) >= :startDate " +
            "   AND COALESCE(cv.voucherdate, apv.voucherdate) < :endDate " +
            "   AND COALESCE(cv.fk_documentstatusid, apv.fk_documentstatusid) = :statusId " +
            "GROUP BY tc.description, tc.code, cvip.percentage " +
            "ORDER BY tc.code", nativeQuery = true)
    List<Object[]> getSummaryOfTaxesWithheld(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             @Param("statusId") Integer statusId);

    @Query(value = "SELECT  " +
            "   tc.code, " +
            "   tc.payeeType AS slType, " +
            "   sl.name, " +
            "   sl.tin, " +
            "   SUM(cvip.baseAmount) AS grossAmount, " +
            "   cvip.percentage / 100 AS taxRate, " +
            "   SUM(cvip.amount) AS taxWithheld " +
            "FROM CheckVoucherIncomePayment cvip " +
            "INNER JOIN TaxCode tc ON tc.id = cvip.FK_taxCodeId " +
            "LEFT JOIN CheckVoucher cv ON cv.fk_transactionid = cvip.fk_transactionid " +
            "LEFT JOIN AccountsPayableVoucher apv ON apv.fk_transactionid = cvip.fk_transactionid " +
            "LEFT JOIN slentity sl ON sl.accountNo = COALESCE(cv.FK_payeeAccountNo, apv.fk_vendoraccountno) " +
            "WHERE COALESCE(cv.voucherdate, apv.voucherdate) >= :startDate " +
            "   AND COALESCE(cv.voucherdate, apv.voucherdate) < :endDate " +
            "   AND COALESCE(cv.fk_documentstatusid, apv.fk_documentstatusid) = :statusId " +
            "GROUP BY tc.code, tc.payeeType, sl.name, sl.tin, cvip.percentage " +
            "ORDER BY tc.code", nativeQuery = true)
    List<Object[]> getSummaryOfTaxesWithheldPerTaxCode(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("statusId") Integer statusId);

}
