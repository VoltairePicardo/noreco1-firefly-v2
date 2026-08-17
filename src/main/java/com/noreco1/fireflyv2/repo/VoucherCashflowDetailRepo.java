package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.VoucherCashflowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface VoucherCashflowDetailRepo extends JpaRepository<VoucherCashflowDetail, Integer> {
    public Long deleteByTransactionId(Integer transId);

    public List<VoucherCashflowDetail> findByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "CashflowItem.name, " +
            "IF(fk_DocumentStatusID = 7, SUM(COALESCE(amount,0)), 0) as amountToDate, " + // approved vouchers only
            "CashflowItem.id, " +
            "CashflowItem.ordinalNumber " +
            "FROM CashflowItem " +
            "LEFT JOIN  VoucherCashflowDetail " +
            " ON CashflowItem.id = VoucherCashflowDetail.FK_cashflowItemId " +
            "  AND voucherDate BETWEEN :from AND :to " +
            "LEFT JOIN allvouchers ON VoucherCashflowDetail.FK_transactionId = allvouchers.fk_transactionid " +
            "WHERE CashflowItem.FK_parentId = :cashflowItemParentId " +
            "GROUP BY CashflowItem.id", nativeQuery = true)
    public List<Object[]> findByCashFlowItemParentIdAndDateRange(@Param("cashflowItemParentId") Integer cashflowItemParentId,
                                                                 @Param("from") String from,
                                                                 @Param("to") String to);

    @Query(value = "SELECT " +
            "CashflowItem.name, " +
            "IF(fk_DocumentStatusID = 7, SUM(COALESCE(amount,0)), 0) as amountToDate, " + // approved vouchers only
            "CashflowItem.id, " +
            "CashflowItem.ordinalNumber " +
            "FROM CashflowItem " +
            "LEFT JOIN  VoucherCashflowDetail " +
            " ON CashflowItem.id = VoucherCashflowDetail.FK_cashflowItemId " +
            "  AND voucherDate <= :cutOff " +
            "LEFT JOIN allvouchers ON VoucherCashflowDetail.FK_transactionId = allvouchers.fk_transactionid " +
            "WHERE CashflowItem.FK_parentId = :cashflowItemParentId " +
            "GROUP BY CashflowItem.id", nativeQuery = true)
    public List<Object[]> findByCashFlowItemParentIdToDate(@Param("cashflowItemParentId") Integer cashflowItemParentId,
                                                                 @Param("cutOff") String cutOff);

    @Query(value = "SELECT " +
            "CashflowItem.id, " +
            "CashflowItem.name, " +
            "IF(fk_DocumentStatusID = 7, SUM(COALESCE(amount,0)), 0) as amountToDate, " +   // approved vouchers only
            "CashflowItem.ordinalNumber " +
            "FROM CashflowItem " +
            "LEFT JOIN  VoucherCashflowDetail " +
            " ON CashflowItem.id = VoucherCashflowDetail.FK_cashflowItemId " +
            "  AND voucherDate BETWEEN :from AND :to " +
            "LEFT JOIN allvouchers ON VoucherCashflowDetail.FK_transactionId = allvouchers.fk_transactionid " +
            "WHERE CashflowItem.FK_cashFlowItemTypeId = :cashflowItemTypeId " +
            "AND CashflowItem.FK_parentId IS NULL " +
            "GROUP BY CashflowItem.id", nativeQuery = true)
    public List<Object[]> findByCashflowItemTypeIdAndDateRangeAndHasNoParent(@Param("cashflowItemTypeId") Integer cashflowItemTypeId,
                                                                             @Param("from") String from,
                                                                             @Param("to") String to);

    @Query(value = "SELECT " +
            " IF(fk_DocumentStatusID = 7, SUM(COALESCE(amount,0)), 0) as amountThisMonth " + // approved vouchers only
            " FROM CashflowItem " +
            " LEFT JOIN  VoucherCashflowDetail " +
            "  ON CashflowItem.id = VoucherCashflowDetail.FK_cashflowItemId " +
            "   AND voucherDate BETWEEN :from AND :to " +
            "LEFT JOIN allvouchers ON VoucherCashflowDetail.FK_transactionId = allvouchers.fk_transactionid " +
            " WHERE CashflowItem.id = :cashflowItemId LIMIT 1", nativeQuery = true)
    Object[] findByCashflowItemIdAndDateRange(@Param("cashflowItemId") Integer cashflowItemId,
                                                     @Param("from") String from,
                                                     @Param("to") String to);

    List<VoucherCashflowDetail> findByGeneralLedgerId(Integer generalLedgerId);

    @Query(value = "SELECT " +
            "SUM(amount) " +
            "FROM " +
            "(SELECT amount, id, parentAccountId " +
            "FROM (SELECT  " +
            " (COALESCE(debit, 0)) - (COALESCE(credit, 0)) amount,  " +
            " Account.id, " +
            " Account.parentAccountId  " +
            " FROM Account  " +
            " LEFT JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId  " +
            " LEFT JOIN ( " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AccountsPayableVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CheckVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN JournalVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN SalesVoucher v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN AdjustmentJournal v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " JOIN SegmentAccount ON FK_segmentAccountId = SegmentAccount.id " +
            " JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " AND Account.parentAccountId = :parentAccountId " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN CashReceipts v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            " UNION " +
            " " +
            " SELECT " +
            " debit, " +
            " credit, " +
            " FK_segmentAccountId " +
            " FROM GeneralLedger " +
            " JOIN MaterialIssueRegister v ON GeneralLedger.FK_transactionId = v.FK_transactionId " +
            " WHERE FK_documentStatusId = :documentStatusId AND v.voucherDate BETWEEN DATE_SUB(:startDate, INTERVAL 1 DAY) AND :endDate " +
            " " +
            ") as vouchers ON SegmentAccount.id = vouchers.FK_segmentAccountId " +
            " ORDER BY Account.parentAccountId, Account.id) accounts, " +
            "(SELECT @pv \\:= :parentAccountId) initialisation " +
            "WHERE FIND_IN_SET(parentAccountId, @pv) > 0 " +
            "AND @pv \\:= CONCAT(@pv, ',', id)) AS t1", nativeQuery = true)
    BigDecimal findCashBalances(@Param("parentAccountId") Integer parentAccountId,
                                @Param("documentStatusId") Integer documentStatusId,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate);

    @Query(value = "SELECT " +
            "VoucherCashflowDetail.voucherDate, code, SUM(amount) as amount " +
            "FROM VoucherCashflowDetail  " +
            "JOIN allvouchers ON VoucherCashflowDetail.FK_transactionId = allvouchers.fk_transactionid   " +
            "WHERE VoucherCashflowDetail.voucherDate BETWEEN :from AND :to " +
            "AND VoucherCashflowDetail.FK_cashflowItemId = :cashflowItemId " +
            "GROUP BY VoucherCashflowDetail.FK_transactionId  " +
            "ORDER BY voucherDate, code", nativeQuery = true)
    List<Object[]> findByCashflowItemIdAndDetailDateRange(@Param("cashflowItemId") Integer cashflowItemId,
                                                          @Param("from") java.util.Date from,
                                                          @Param("to") java.util.Date to);

    @Query(value = "SELECT * FROM VoucherCashflowDetail " +
            "WHERE VoucherCashflowDetail.FK_transactionId = :transId " +
            "GROUP BY VoucherCashflowDetail.FK_cashflowItemId", nativeQuery = true)
    List<Object[]> findByTransactionIdGroupByCashFlowItem(@Param("transId") Integer transId);

    @Query(value = "SELECT CashflowItem.id, name, sum(amount) as amount FROM VoucherCashflowDetail a " +
            "JOIN GeneralLedger gl on a.FK_generalLedgerId = gl.id " +
            "JOIN SegmentAccount sa on gl.FK_segmentAccountId = sa.id " +
            "JOIN CashflowItem ON a.FK_cashflowItemId = CashflowItem.id " +
            "where a.FK_transactionId = :transId and sa.FK_accountId = :accountId " +
            "GROUP BY a.FK_cashflowItemId ", nativeQuery = true)
    List<Object[]> findByTransactionIdAndAccountIdGroupByCashFlowItem(@Param("transId") Integer transId,
                                                                      @Param("accountId") Integer accountId);

    @Query(value = "SELECT sum(amount) as amount FROM VoucherCashflowDetail a " +
            "JOIN GeneralLedger gl on a.FK_generalLedgerId = gl.id " +
            "JOIN SegmentAccount sa on gl.FK_segmentAccountId = sa.id  " +
            "where a.FK_transactionId = :transId " +
            "AND a.FK_cashflowItemId = :cfItemId " +
            "AND sa.FK_accountId = :accountId " +
            "LIMIT 1", nativeQuery = true)
    List<Object[]> sumByTransIdAndCashFlowItemId(@Param("transId") Integer transId,
                                                 @Param("cfItemId") Integer cfItemId,
                                                 @Param("accountId") Integer accountId);

    Long deleteByTransactionIdAndGeneralLedgerSegmentAccountAccountId(Integer transId, Integer accountId);

    @Query(value = "SELECT CashflowItem.id, name from VoucherCashflowDetail a " +
            "JOIN CashflowItem ON a.FK_cashflowItemId = CashflowItem.id " +
            "WHERE a.voucherDate  BETWEEN :from AND :to " +
            "GROUP BY CashflowItem.id ORDER BY name", nativeQuery = true)
    List<Object[]> findCashFlowItemsWithEntries(@Param("from") java.util.Date from, @Param("to") java.util.Date to);

    @Query(value = "SELECT CashflowItem.id, name, sum(amount) as amount FROM VoucherCashflowDetail a " +
            "JOIN CashflowItem ON a.FK_cashflowItemId = CashflowItem.id " +
            "where a.FK_transactionId = :transId  " +
            "GROUP BY a.FK_cashflowItemId  ", nativeQuery = true)
    List<Object[]> findByTransactionGroupByCashFlowItem(@Param("transId") Integer transId);

}
