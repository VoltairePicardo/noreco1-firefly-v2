package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface GeneralLedgerRepo extends JpaRepository<GeneralLedger, Integer> {
    public List<GeneralLedger> findByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "debit, " +
            "credit, " +
            "Account.id, " +
            "Account.code, " +
            "Account.title, " +
            "gl.id  as glId " +
            "FROM(SELECT " +
            "SegmentAccount.FK_accountId as accountId, " +
            "SUM(GeneralLedger.debit) AS debit, " +
            "SUM(GeneralLedger.credit) AS credit, " +
            "MIN(GeneralLedger.id) AS id, " +
            "IF(debit-credit > 0 , 1, 2) as side " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "GROUP BY SegmentAccount.FK_accountId, side " +
            "ORDER BY id) as gl  " +
            "JOIN Account ON gl.accountId = Account.id", nativeQuery = true)
    public List<Object[]> findByTransactionIdGroupByAccount(@Param("transId") Integer transId);

    @Query(value = "SELECT " +
            "SUM(debit) AS debit, " +
            "SUM(credit) AS credit, " +
            "Account.id, " +
            "Account.code, " +
            "Account.title, " +
            "MIN(gl.id) as glId " +
            "FROM(SELECT " +
            "SegmentAccount.FK_accountId as accountId, " +
            "SUM(GeneralLedger.debit) AS debit, " +
            "SUM(GeneralLedger.credit) AS credit, " +
            "MIN(GeneralLedger.id) AS id, " +
            "IF(debit-credit > 0 , 1, 2) as side " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "GROUP BY SegmentAccount.FK_accountId, side " +
            "ORDER BY id) as gl  " +
            "JOIN Account ON gl.accountId = Account.id " +
            "LEFT JOIN TaxCode ON Account.id = TaxCode.FK_accountId " +
            "WHERE TaxCode.id is null " +
            "GROUP BY Account.id", nativeQuery = true)
    public List<Object[]> findByTransactionIdWithoutTaxGroupByAccount(@Param("transId") Integer transId);

    public Long deleteByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "GeneralLedger.*, " +
            "BusinessSegment.description " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId = BusinessSegment.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "AND SegmentAccount.FK_accountId = :accountId", nativeQuery = true)
    public List<Object[]> findByTransactionIdAndAccount(@Param("transId") Integer transId, @Param("accountId") Integer accountId);

    @Query(value = "SELECT " +
            "SegmentAccount.accountCode, " +
            "SUM(COALESCE(GeneralLedger.debit, 0)) debit, " +
            "SUM(COALESCE(GeneralLedger.credit, 0)) credit, " +
            "Account.title, " +
            "if (debit-credit > 0 , 1, 2) as side " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "GROUP BY Account.code " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterByTransId(@Param("transId") Integer transId);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, ((COALESCE(debit, 0)) - (COALESCE(credit, 0))), ((COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " SegmentAccount.FK_businessSegmentId as businessSegmentId,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " Factor.code AS fCode " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN AllocationFactor ON Account.id = AllocationFactor.FK_accountId " +
            " LEFT JOIN Factor ON Factor.id = AllocationFactor.FK_factorId " +
            " LEFT JOIN ( " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate  " +
            " AND Account.parentAccountId = :parentAccountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            " WHERE Account.`parentAccountId` = :parentAccountId " +
            " ORDER BY Account.code ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountId(@Param("parentAccountId") Integer parentAccountId,
                                                @Param("documentStatusId") Integer documentStatusId,
                                                @Param("voucherDate") Date voucherDate);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, ((COALESCE(debit, 0)) - (COALESCE(credit, 0))), ((COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " SegmentAccount.FK_businessSegmentId as businessSegmentId,   " +
            " Account.id, " +
            " Account.FK_accountTypeId   " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate  " +
            " AND Account.id = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :voucherDate " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            " WHERE Account.`id` = :accountId " +
            " ORDER BY Account.code ASC", nativeQuery = true)
    public List<Object[]> findByAccountIdForBalanceSheet(@Param("accountId") Integer accountId,
                                                            @Param("documentStatusId") Integer documentStatusId,
                                                            @Param("voucherDate") Date voucherDate);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, ((COALESCE(debit, 0)) - (COALESCE(credit, 0))), ((COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " SegmentAccount.FK_businessSegmentId as businessSegmentId,   " +
            " Account.id, " +
            " Account.FK_accountTypeId   " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId," +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId," +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId," +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId," +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId," +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            " AND Account.parentAccountId = :parentAccountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            "GeneralLedger.id as glid " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startDate AND :endDate " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            " WHERE Account.`parentAccountId` = :parentAccountId " +
            " ORDER BY Account.code ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdAndVoucherDateRange(@Param("parentAccountId") Integer parentAccountId,
                                                                   @Param("documentStatusId") Integer documentStatusId,
                                                                   @Param("startDate") Date startDate,
                                                                   @Param("endDate") Date endDate);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0))  as debit,  " +
            " SUM(COALESCE(credit, 0))  as credit,   " +
            " Account.normalBalance, " +
            " Account.isHeader " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.transactionType IN (:transTypes) " +
            " AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbTransactionsNEA(@Param("parentAccountId") Integer parentAccountId,
                                                                    @Param("documentStatusId") Integer documentStatusId,
                                                                    @Param("startOfCutoff") Date startOfCutoff,
                                                                    @Param("endOfCutoff") Date endOfCutoff,
                                                                    @Param("transTypes") List<String> transTypes );

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0))  as debit,  " +
            " SUM(COALESCE(credit, 0))  as credit,   " +
            " Account.normalBalance, " +
            " Account.isHeader " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId " +
            " AND v.id NOT IN ( SELECT ajv.id FROM AdjustmentJournal ajv WHERE ajv.voucherDate >= :cutOffDate AND v.transactionType IN (:transTypes))" +
            " AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbNEAUnaudited(@Param("parentAccountId") Integer parentAccountId,
                                                                    @Param("documentStatusId") Integer documentStatusId,
                                                                    @Param("cutOffDate") String cutOffDate,
                                                                    @Param("transTypes") List<String> transTypes );

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0))  as debit,  " +
            " SUM(COALESCE(credit, 0))  as credit,   " +
            " Account.normalBalance, " +
            " Account.isHeader " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId " +
            " AND v.voucherDate = :cutOffDate AND v.transactionType IN (:transTypes) " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbNEAAdjustment(@Param("parentAccountId") Integer parentAccountId,
                                                                 @Param("documentStatusId") Integer documentStatusId,
                                                                 @Param("cutOffDate") String cutOffDate,
                                                                 @Param("transTypes") List<String> transTypes );

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0))  as debit,  " +
            " SUM(COALESCE(credit, 0))  as credit,   " +
            " Account.normalBalance, " +
            " Account.isHeader " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId " +
            " AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOffDate " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbNEAAudited(@Param("parentAccountId") Integer parentAccountId,
                                                                 @Param("documentStatusId") Integer documentStatusId,
                                                                 @Param("cutOffDate") String cutOffDate);

    @Query(value = "SELECT " +
            "SUM(IF(debit>credit, debit-credit, 0)) AS debit, " +
            "SUM(IF(debit<credit, credit-debit, 0)) AS credit " +
            "FROM (" +
            " SELECT   " +
            " SUM(COALESCE(debit, 0))  as debit, " +
            " SUM(COALESCE(credit, 0))  as credit " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.transactionType IN (:transTypes) " +
            " AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN BankDeposit v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :startOfCutoff AND :endOfCutoff " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.isHeader = 0 " +
            "GROUP BY Account.`code` " +
            ") AS transactions WHERE debit > 0 OR credit > 0", nativeQuery = true)
    public List<Object[]> getTransactionsGrandTotalForTbNEA(@Param("documentStatusId") Integer documentStatusId,
                                                            @Param("startOfCutoff") Date startOfCutoff,
                                                            @Param("endOfCutoff") Date endOfCutoff,
                                                            @Param("transTypes") List<String> transTypes );

    @Query(value = "SELECT " +
            "SUM(IF(debit>credit, debit-credit, 0)) AS debit, " +
            "SUM(IF(debit<credit, credit-debit, 0)) AS credit " +
            "FROM (" +
            " SELECT   " +
            " SUM(COALESCE(debit, 0))  as debit, " +
            " SUM(COALESCE(credit, 0))  as credit " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId  AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN BankDeposit v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId  " +
            "WHERE Account.isHeader = 0 " +
            "GROUP BY Account.`code` " +
            ") AS transactions WHERE debit > 0 OR credit > 0", nativeQuery = true)
    public List<Object[]> getEndBalancesGrandTotalForTbNEA(@Param("documentStatusId") Integer documentStatusId,
                                                            @Param("cutoff") Date cutoff);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " IF(Account.normalBalance = 1, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0))) amount,   " +
            " SegmentAccount.FK_businessSegmentId as businessSegmentId,   " +
            " Account.id, " +
            " Account.FK_accountTypeId   " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            "  " +
            " UNION " +
            "  " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId, " +
            " GeneralLedger.id " +
            " FROM GeneralLedger " +
            " JOIN BankDeposit v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :fromDate AND :toDate " +
            " AND SegmentAccount.FK_accountId = :accountId " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId " +
            " WHERE Account.`id` = :accountId " +
            " ORDER BY Account.code ASC", nativeQuery = true)
    public List<Object[]> findByAccountId(@Param("accountId") Integer accountId,
                                          @Param("documentStatusId") Integer documentStatusId,
                                          @Param("fromDate") Date fromDate,
                                          @Param("toDate") Date toDate);

    @Query(value = "SELECT " +
            "Account.title, " +
            "sum(GeneralLedger.debit) as debit, " +
            "sum(GeneralLedger.credit) as credit, " +
            "Account.code " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "GROUP BY SegmentAccount.FK_accountId ORDER BY debit DESC", nativeQuery = true)
    List<Object[]> sumDebitCreditByTransId(@Param("transId") Integer transId);

    // ------------------------------ GL Account Inquiry --------------------------------------

    @Query(value = "SELECT  " +
            " documents2.*,   " +
            " @sm \\:= @sm + IF(documents2.normalBalance = 1, sumDebit-sumCredit , (sumCredit-sumDebit)) as balance  " +
            " FROM (SELECT @sm \\:= :beginningBalance) r, (  " +
            "   SELECT   " +
            "   documents.*,   " +
            "   SUM(coalesce(debit,0)) sumDebit,  " +
            "   SUM(coalesce(credit,0)) sumCredit, " +
            "   Account.normalBalance " +
            "   FROM (   " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       1 as uniqueId " +
            "       FROM CheckVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       2 as uniqueId " +
            "       FROM CashReceipts   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION    " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       3 as uniqueId " +
            "       FROM MaterialIssueRegister   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       explanation as particulars,   " +
            "       FK_transactionId as transId,   " +
            "       4 as uniqueId " +
            "       FROM JournalVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId  " +
            "        " +
            "       UNION     " +
            "      " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       5 as uniqueId " +
            "       FROM AccountsPayableVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId   " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       6 as uniqueId " +
            "       FROM SalesVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       8 as uniqueId " +
            "       FROM OtherAccountReceivable   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "   ) as documents   " +
            "   JOIN GeneralLedger ON documents.transId = GeneralLedger.FK_transactionId     " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "   WHERE Account.id = :accountId " +
            "   GROUP BY documents.transId " +
            "   ORDER BY uniqueId asc, document_date asc, created_date asc  " +
            ") as documents2", nativeQuery = true)
    List<Object[]> findAllForGLInquiry(@Param("accountId") Integer accountId,
                                       @Param("documentStatusId") Integer documentStatusId,
                                       @Param("from") Date from,
                                       @Param("to") Date to,
                                       @Param("beginningBalance") BigDecimal beginningBalance
    );

    @Query(value = "SELECT  " +
            " documents2.*,   " +
            " @sm \\:= @sm + IF(documents2.normalBalance = 1, sumDebit-sumCredit , (sumCredit-sumDebit)) as balance  " +
            " FROM (SELECT @sm \\:= :beginningBalance) r, (  " +
            "   SELECT   " +
            "   documents.*,   " +
            "   SUM(coalesce(debit,0)) sumDebit,  " +
            "   SUM(coalesce(credit,0)) sumCredit, " +
            "   Account.normalBalance " +
            "   FROM (   " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       1 as uniqueId " +
            "       FROM CheckVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       2 as uniqueId " +
            "       FROM CashReceipts   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION    " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       3 as uniqueId " +
            "       FROM MaterialIssueRegister   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       explanation as particulars,   " +
            "       FK_transactionId as transId,   " +
            "       4 as uniqueId " +
            "       FROM JournalVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId  " +
            "        " +
            "       UNION     " +
            "      " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       5 as uniqueId " +
            "       FROM AccountsPayableVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId   " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       6 as uniqueId " +
            "       FROM SalesVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       8 as uniqueId " +
            "       FROM OtherAccountReceivable   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "   ) as documents   " +
            "   JOIN GeneralLedger ON documents.transId = GeneralLedger.FK_transactionId     " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "   WHERE Account.id = :accountId " +
            "   GROUP BY documents.transId " +
            "   ORDER BY uniqueId asc, document_date asc, created_date asc  " +
            ") as documents2", nativeQuery = true)
    List<Object[]> findAllForGLInquiryAll(@Param("accountId") Integer accountId,
                                       @Param("documentStatusId") Integer documentStatusId,
                                       @Param("from") Date from,
                                       @Param("to") Date to,
                                       @Param("beginningBalance") BigDecimal beginningBalance
    );

    @Query(value = "SELECT " +
            "Account.normalBalance, " +
            "sum(coalesce(debit,0)) as sumDebit,   " +
            "sum(coalesce(credit,0)) as sumCredit " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "WHERE Account.id = :accountId " +
            "AND GeneralLedger.FK_transactionId IN ( " +
            "   SELECT " +
            "   * " +
            "   FROM (SELECT " +
            "   FK_transactionId as transId " +
            "   FROM AccountsPayableVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CheckVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM JournalVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CashReceipts " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION  " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM SalesVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "    SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM MaterialIssueRegister  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "     " +
            "     " +
            "    UNION " +
            "     " +
            "   SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM OtherAccountReceivable  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    ) as trans " +
            ") GROUP BY Account.id LIMIT 1  ", nativeQuery = true)
    List<Object[]> findBeginningBalanceForGLInquiry(@Param("accountId") Integer accountId,
                                                    @Param("documentStatusId") Integer documentStatusId,
                                                    @Param("from") Date from
    );

    @Query(value = "SELECT " +
            "Account.normalBalance, " +
            "sum(coalesce(debit,0)) as sumDebit,   " +
            "sum(coalesce(credit,0)) as sumCredit " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "WHERE Account.id = :accountId " +
            "AND GeneralLedger.FK_transactionId IN ( " +
            "   SELECT " +
            "   * " +
            "   FROM (SELECT " +
            "   FK_transactionId as transId " +
            "   FROM AccountsPayableVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CheckVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM JournalVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CashReceipts " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION  " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM SalesVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "    SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM MaterialIssueRegister  " +
            "    WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "     " +
            "     " +
            "    UNION " +
            "     " +
            "   SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM OtherAccountReceivable  " +
            "    WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    ) as trans " +
            ") GROUP BY Account.id LIMIT 1  ", nativeQuery = true)
    List<Object[]> findBeginningBalanceForGLInquiryAll(@Param("accountId") Integer accountId,
                                                    @Param("documentStatusId") Integer documentStatusId,
                                                    @Param("from") Date from
    );

    // ------------------------------ GL Account Inquiry Summary --------------------------------------

    @Query(value = "SELECT  " +
            " documents2.*,   " +
            " @sm \\:= @sm + IF(documents2.normalBalance = 1, sumDebit-sumCredit , (sumCredit-sumDebit)) as balance  " +
            " FROM (SELECT @sm \\:= :beginningBalance) r, (  " +
            "   SELECT   " +
            "   documents.*,   " +
            "   SUM(coalesce(debit,0)) sumDebit,  " +
            "   SUM(coalesce(credit,0)) sumCredit, " +
            "   Account.normalBalance " +
            "   FROM (   " +
            "       SELECT   " +
            "       'Check Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       1 as uniqueId " +
            "       FROM CheckVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Cash Receipts' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       2 as uniqueId " +
            "       FROM CashReceipts   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION    " +
            "        " +
            "       SELECT    " +
            "       'Material Issue Register' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       3 as uniqueId " +
            "       FROM MaterialIssueRegister   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Journal Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       explanation as particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       4 as uniqueId " +
            "       FROM JournalVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId  " +
            "        " +
            "       UNION     " +
            "      " +
            "       SELECT   " +
            "       'Accounts Payable Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       5 as uniqueId " +
            "       FROM AccountsPayableVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId   " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Sales Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       6 as uniqueId " +
            "       FROM SalesVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT    " +
            "       'Other Accounts Receivable' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       8 as uniqueId " +
            "       FROM OtherAccountReceivable   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "   ) as documents   " +
            "   JOIN GeneralLedger ON documents.transId = GeneralLedger.FK_transactionId     " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "   WHERE Account.id = :accountId " +
            "   GROUP BY uniqueId, documents.month " +
            "   ORDER BY document_date asc, uniqueId asc, created_date asc  " +
            ") as documents2 ", nativeQuery = true)
    List<Object[]> findAllForGLInquirySummary(@Param("accountId") Integer accountId,
                                              @Param("documentStatusId") Integer documentStatusId,
                                              @Param("from") Date from,
                                              @Param("to") Date to,
                                              @Param("beginningBalance") BigDecimal beginningBalance
    );

    @Query(value = "SELECT  " +
            " documents2.*,   " +
            " @sm \\:= @sm + IF(documents2.normalBalance = 1, sumDebit-sumCredit , (sumCredit-sumDebit)) as balance  " +
            " FROM (SELECT @sm \\:= :beginningBalance) r, (  " +
            "   SELECT   " +
            "   documents.*,   " +
            "   SUM(coalesce(debit,0)) sumDebit,  " +
            "   SUM(coalesce(credit,0)) sumCredit, " +
            "   Account.normalBalance " +
            "   FROM (   " +
            "       SELECT   " +
            "       'Check Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       1 as uniqueId " +
            "       FROM CheckVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Cash Receipts' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       2 as uniqueId " +
            "       FROM CashReceipts   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION    " +
            "        " +
            "       SELECT    " +
            "       'Material Issue Register' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       3 as uniqueId " +
            "       FROM MaterialIssueRegister   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Journal Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       explanation as particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       4 as uniqueId " +
            "       FROM JournalVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId  " +
            "        " +
            "       UNION     " +
            "      " +
            "       SELECT   " +
            "       'Accounts Payable Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       5 as uniqueId " +
            "       FROM AccountsPayableVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId   " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       'Sales Voucher' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       6 as uniqueId " +
            "       FROM SalesVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT    " +
            "       'Other Accounts Receivable' as reference,   " +
            "       LAST_DAY(voucherDate) as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       MONTH(voucherDate) as month,   " +
            "       8 as uniqueId " +
            "       FROM OtherAccountReceivable   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId != :documentStatusId " +
            "   ) as documents   " +
            "   JOIN GeneralLedger ON documents.transId = GeneralLedger.FK_transactionId     " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "   WHERE Account.id = :accountId " +
            "   GROUP BY uniqueId, documents.month " +
            "   ORDER BY document_date asc, uniqueId asc, created_date asc  " +
            ") as documents2 ", nativeQuery = true)
    List<Object[]> findAllForGLInquirySummaryAll(@Param("accountId") Integer accountId,
                                              @Param("documentStatusId") Integer documentStatusId,
                                              @Param("from") Date from,
                                              @Param("to") Date to,
                                              @Param("beginningBalance") BigDecimal beginningBalance
    );

    @Query(value = "SELECT " +
            "Account.normalBalance, " +
            "sum(coalesce(debit,0)) as sumDebit,   " +
            "sum(coalesce(credit,0)) as sumCredit " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "WHERE Account.id = :accountId " +
            "AND GeneralLedger.FK_transactionId IN ( " +
            "   SELECT " +
            "   * " +
            "   FROM (SELECT " +
            "   FK_transactionId as transId " +
            "   FROM AccountsPayableVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CheckVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM JournalVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CashReceipts " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION  " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM SalesVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "    SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM MaterialIssueRegister  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "     " +
            "     " +
            "    UNION " +
            "     " +
            "   SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM OtherAccountReceivable  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    ) as trans " +
            ") GROUP BY Account.id LIMIT 1  ", nativeQuery = true)
    List<Object[]> findBeginningBalanceForGLInquirySummary(@Param("accountId") Integer accountId,
                                                           @Param("documentStatusId") Integer documentStatusId,
                                                           @Param("from") Date from
    );

    @Query(value = "SELECT " +
            "Account.normalBalance, " +
            "sum(coalesce(debit,0)) as sumDebit,   " +
            "sum(coalesce(credit,0)) as sumCredit " +
            "FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "WHERE Account.id = :accountId " +
            "AND GeneralLedger.FK_transactionId IN ( " +
            "   SELECT " +
            "   * " +
            "   FROM (SELECT " +
            "   FK_transactionId as transId " +
            "   FROM AccountsPayableVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CheckVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM JournalVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CashReceipts " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION  " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM SalesVoucher " +
            "   WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "    SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM MaterialIssueRegister  " +
            "    WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "     " +
            "     " +
            "    UNION " +
            "     " +
            "   SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM OtherAccountReceivable  " +
            "    WHERE FK_documentStatusId != :documentStatusId AND voucherDate < :from " +
            "    ) as trans " +
            ") GROUP BY Account.id LIMIT 1  ", nativeQuery = true)
    List<Object[]> findBeginningBalanceForGLInquirySummaryAll(@Param("accountId") Integer accountId,
                                                           @Param("documentStatusId") Integer documentStatusId,
                                                           @Param("from") Date from
    );

    // ------------------------------ SL Account Inquiry --------------------------------------

    @Query(value = "SELECT  " +
            " documents2.*,   " +
            " @sm \\:= @sm + IF(documents2.normalBalance = 1, sumDebit-sumCredit , (sumCredit-sumDebit)) as balance  " +
            " FROM (SELECT @sm \\:= :beginningBalance) r, (  " +
            "   SELECT   " +
            "   documents.*,   " +
            "   SUM(coalesce(debit,0)) sumDebit,  " +
            "   SUM(coalesce(credit,0)) sumCredit, " +
            "   Account.normalBalance " +
            "   FROM (   " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       1 as uniqueId " +
            "       FROM CheckVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       2 as uniqueId " +
            "       FROM CashReceipts   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION    " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       3 as uniqueId " +
            "       FROM MaterialIssueRegister   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       explanation as particulars,   " +
            "       FK_transactionId as transId,   " +
            "       4 as uniqueId " +
            "       FROM JournalVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId  " +
            "        " +
            "       UNION     " +
            "      " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       5 as uniqueId " +
            "       FROM AccountsPayableVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId   " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT   " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       6 as uniqueId " +
            "       FROM SalesVoucher   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "        " +
            "       UNION   " +
            "        " +
            "       SELECT    " +
            "       code as reference,   " +
            "       voucherDate as document_date,   " +
            "       createdAt as created_date,   " +
            "       particulars,   " +
            "       FK_transactionId as transId,   " +
            "       8 as uniqueId " +
            "       FROM OtherAccountReceivable   " +
            "       WHERE voucherDate BETWEEN :from AND :to AND FK_documentStatusId = :documentStatusId " +
            "   ) as documents   " +
            "   JOIN SubLedger ON documents.transId = SubLedger.FK_transactionId     " +
            "   JOIN SLEntity ON SubLedger.FK_accountNo = SLEntity.accountNo  " +
            "   JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "   WHERE Account.id = :accountId AND SLEntity.accountNo = :accountNo " +
            "   GROUP BY documents.transId " +
            "   ORDER BY uniqueId asc, document_date asc, created_date asc  " +
            ") as documents2", nativeQuery = true)
    List<Object[]> findAllForSLInquiry(@Param("accountId") Integer accountId,
                                       @Param("accountNo") Integer accountNo,
                                       @Param("documentStatusId") Integer documentStatusId,
                                       @Param("from") Date from,
                                       @Param("to") Date to,
                                       @Param("beginningBalance") BigDecimal beginningBalance
    );

    @Query(value = "SELECT " +
            "Account.normalBalance, " +
            "sum(coalesce(debit,0)) as sumDebit,   " +
            "sum(coalesce(credit,0)) as sumCredit " +
            "FROM SubLedger " +
            "JOIN SLEntity ON SubLedger.FK_accountNo = SLEntity.accountNo " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "WHERE Account.id = :accountId AND SLEntity.accountNo = :accountNo " +
            "AND SubLedger.FK_transactionId IN ( " +
            "   SELECT " +
            "   * " +
            "   FROM (SELECT " +
            "   FK_transactionId as transId " +
            "   FROM AccountsPayableVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CheckVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM JournalVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM CashReceipts " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION  " +
            "    " +
            "    " +
            "   SELECT " +
            "   FK_transactionId as transId " +
            "   FROM SalesVoucher " +
            "   WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    " +
            "    " +
            "   UNION " +
            "    " +
            "    " +
            "    SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM MaterialIssueRegister  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "     " +
            "     " +
            "    UNION " +
            "     " +
            "   SELECT  " +
            "    FK_transactionId as transId  " +
            "    FROM OtherAccountReceivable  " +
            "    WHERE FK_documentStatusId = :documentStatusId AND voucherDate < :from " +
            "    ) as trans " +
            ") GROUP BY Account.id LIMIT 1  ", nativeQuery = true)
    List<Object[]> findBeginningBalanceForSLInquiry(@Param("accountId") Integer accountId,
                                                    @Param("accountNo") Integer accountNo,
                                                    @Param("documentStatusId") Integer documentStatusId,
                                                    @Param("from") Date from
    );

    @Query(value = "SELECT " +
            "SUM(IF(Account.normalBalance = 1, COALESCE(debit, 0) - COALESCE(credit, 0), COALESCE(credit, 0) - COALESCE(debit, 0))) amount " +
            "FROM Account " +
            "LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to  " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId " +
            " AND v.transactionType IN (:transTypes) AND v.voucherDate BETWEEN :from AND :to  " +
            "  " +
//            " UNION ALL " +
//            "  " +
//            " SELECT " +
//            " GeneralLedger.id, " +
//            " debit, " +
//            " credit, " +
//            " FK_segmentAccountId " +
//            " FROM GeneralLedger " +
//            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
//            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
//            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
//            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :from AND Account.id = :accountId " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to  " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to  " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " IF(:getDebit=true, debit, 0) AS debit, " +
            " IF(:getCredit=true, credit, 0) AS credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN BankDeposit v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE v.FK_workflowId IN (:workFlowIds) AND FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN :from AND :to  " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId    " +
            " " +
            "WHERE Account.`id` = :accountId ORDER BY Account.code ASC", nativeQuery = true)
    BigDecimal incomeStatementNeaAmountByAccountIdAndRange(@Param("accountId") Integer accountId,
                                                           @Param("from") Date from,
                                                           @Param("to") Date to,
                                                           @Param("documentStatusId") Integer documentStatusId,
                                                           @Param("workFlowIds") List<Integer> workFlowIds,
                                                           @Param("getDebit") Boolean getDebit,
                                                           @Param("getCredit") Boolean getCredit,
                                                           @Param("transTypes") List<String> transTypes
    );

    @Query(value = "SELECT " +
            "SUM(CASE  " +
            "  WHEN normalBalance = 1 THEN COALESCE(debit, 0) - COALESCE(credit, 0) " +
            "  WHEN normalBalance = 2 THEN COALESCE(credit, 0) - COALESCE(debit, 0) " +
            "  ELSE 0 " +
            "END) amount " +
            "FROM Account " +
            "LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff  " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.transactionType IN (:transTypes) AND v.voucherDate BETWEEN :startDate AND :cutOff  " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.year < :year  AND Account.id = :accountId " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff  " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutOff  " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId    " +
            " " +
            "WHERE Account.`id` = :accountId ORDER BY Account.code ASC", nativeQuery = true)
    BigDecimal balanceSheetNeaAmountByAccountIdAndCutOff(@Param("accountId") Integer accountId,
                                                         @Param("cutOff") Date cutOff,
                                                         @Param("documentStatusId") Integer documentStatusId,
                                                         @Param("year") Integer year,
                                                         @Param("transTypes") List<String> transTypes,
                                                         @Param("startDate") Date startDate
    );

    @Query(value = "SELECT " +
            "SegmentAccount.accountCode, " +
            "SUM(COALESCE(GeneralLedger.debit, 0)) debit, " +
            "SUM(COALESCE(GeneralLedger.credit, 0)) credit, " +
            "Account.title, " +
            "if (debit-credit > 0 , 1, 2) as side " +
            "FROM GeneralLedger " +
            "JOIN MaterialIssueRegister ON GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id " +
            "WHERE GeneralLedger.FK_transactionId = :transId " +
            "AND MaterialIssueRegister.inventoryDocType = :invDocType " +
            "GROUP BY Account.code " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterByTransIdAndInvDocType(@Param("transId") Integer transId, @Param("invDocType") String invDocType);

    @Query(value = "SELECT " +
            "wo.id, " +
            "wo.code, " +
            "wo.date, " +
            "wo.description, " +
            "vouchers.FK_transactionId, " +
            "vouchers.voucherDate, " +
            "vouchers.voucherCode, " +
            "vouchers.particulars, " +
            "a.code as acctCode, " +
            "a.title, " +
            "gl.debit, " +
            "gl.credit  " +
            "FROM workorderdetail wod " +
            "INNER JOIN workorder wo ON wo.id = wod.FK_workOrderId " +
            "INNER JOIN generalledger gl ON gl.FK_transactionId = wod.FK_transactionId " +
            "INNER JOIN segmentaccount sa ON sa.id = gl.FK_segmentAccountId " +
            "INNER JOIN account a ON a.id = sa.FK_accountId " +
            "INNER JOIN (SELECT " +
            "cv.voucherDate, " +
            "cv.code as voucherCode, " +
            "cv.particulars, " +
            "cv.FK_transactionId " +
            "FROM checkvoucher cv " +
            "UNION " +
            "SELECT " +
            "jv.voucherDate, " +
            "jv.code as voucherCode, " +
            "jv.explanation as particulars, " +
            "jv.FK_transactionId " +
            "FROM journalvoucher jv " +
            "UNION " +
            "SELECT " +
            "mir.voucherDate, " +
            "mir.code as voucherCode, " +
            "mir.particulars, " +
            "mir.FK_transactionId " +
            "FROM materialissueregister mir) vouchers ON vouchers.FK_transactionId = gl.FK_transactionId " +
            "WHERE vouchers.voucherDate >= :from AND vouchers.voucherDate <= :to " +
            "ORDER BY vouchers.voucherDate, wo.code, vouchers.voucherCode, gl.debit desc, gl.credit desc, a.code", nativeQuery = true)
    List<Object[]> findWorkOrderTransactions(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "a.code, " +
            "a.title, " +
            "sum(gl.debit) as debitAmt, " +
            "sum(gl.credit) as creditAmt " +
            "FROM workorderdetail wod " +
            "INNER JOIN workorder wo ON wo.id = wod.FK_workOrderId " +
            "INNER JOIN generalledger gl ON gl.FK_transactionId = wod.FK_transactionId " +
            "INNER JOIN segmentaccount sa ON sa.id = gl.FK_segmentAccountId " +
            "INNER JOIN account a ON a.id = sa.FK_accountId " +
            "INNER JOIN (SELECT " +
            "cv.FK_transactionId " +
            "FROM checkvoucher cv " +
            "UNION " +
            "SELECT " +
            "jv.FK_transactionId " +
            "FROM journalvoucher jv " +
            "UNION " +
            "SELECT " +
            "mir.FK_transactionId " +
            "FROM materialissueregister mir) vouchers ON vouchers.FK_transactionId = gl.FK_transactionId " +
            "WHERE wo.date >= :from AND wo.date <= :to " +
            "GROUP BY a.id " +
            "ORDER BY debitAmt desc, creditAmt desc, a.code", nativeQuery = true)
    List<Object[]> findWorkOrderTransactionsRecap(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0)) debit,   " +
            " SUM(COALESCE(credit, 0)) credit,  " +
            " Account.normalBalance " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff  " +
            " AND Account.parentAccountId = :parentAccountId " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate < :cutoff " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbBegBalanceNEA(@Param("parentAccountId") Integer parentAccountId,
                                                                  @Param("documentStatusId") Integer documentStatusId,
                                                                  @Param("cutoff") Date cutoff);

    @Query(value = "SELECT   " +
            " Account.code,   " +
            " Account.title,   " +
            " SUM(IF(Account.FK_accountTypeId = 1 OR Account.FK_accountTypeId = 5, (COALESCE(debit, 0)) - (COALESCE(credit, 0)), (COALESCE(credit, 0)) - (COALESCE(debit, 0)))) amount,   " +
            " Account.id, " +
            " Account.FK_accountTypeId,   " +
            " SUM(COALESCE(debit, 0)) debit,   " +
            " SUM(COALESCE(credit, 0)) credit,  " +
            " Account.normalBalance " +
            " FROM Account   " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId   " +
            " LEFT JOIN ( " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff  " +
            " AND Account.parentAccountId = :parentAccountId " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            " UNION ALL  " +
            "  " +
            " SELECT " +
            " GeneralLedger.id, " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate <= :cutoff " +
            "  " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId      " +
            "WHERE Account.`parentAccountId` = :parentAccountId " +
            "GROUP BY Account.Code " +
            "ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 1), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 2), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 3), '-', -1) AS UNSIGNED) ASC, " +
            "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(Account.code,'-', 4), '-', -1) AS UNSIGNED) ASC", nativeQuery = true)
    public List<Object[]> findByParentAccountIdForTbEndBalanceNEA(@Param("parentAccountId") Integer parentAccountId,
                                                                  @Param("documentStatusId") Integer documentStatusId,
                                                                  @Param("cutoff") Date cutoff);

    List<GeneralLedger> findBySegmentAccountId(Integer segmentAccountId);

    @Query(value = "SELECT Account.id, code, title, SUM(GeneralLedger.debit) AS debit  FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "WHERE SegmentAccount.FK_accountId != :accountId " +
            "AND GeneralLedger.FK_transactionId = :voucherTransNo " +
            "AND debit > 0 " +
            "GROUP BY SegmentAccount.FK_accountId",
            nativeQuery = true)
    List<Object[]> findSumDebitWhereAccountNoEqual(@Param("voucherTransNo") Integer voucherTransNo,
                                              @Param("accountId") Integer accountId);

    @Query(value = "SELECT Account.id, code, title, SUM(GeneralLedger.debit) AS debit  FROM GeneralLedger " +
            "JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "WHERE GeneralLedger.FK_transactionId = :voucherTransNo " +
            "AND debit > 0 " +
            "GROUP BY SegmentAccount.FK_accountId",
            nativeQuery = true)
    List<Object[]> findSumDebitByAccountNo(@Param("voucherTransNo") Integer voucherTransNo);

    List<GeneralLedger> findByTransactionIdAndSegmentAccountAccountId(Integer transId, Integer accountId);
}
