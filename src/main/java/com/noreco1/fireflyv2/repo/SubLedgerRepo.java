package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SubLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubLedgerRepo extends JpaRepository<SubLedger, Integer> {
    public List<SubLedger> findByGeneralLedgerId(Integer glId);
    public List<SubLedger> findByTransactionId(Integer transId);
    public Long deleteByTransactionId(Integer transId);
    public List<SubLedger> findByTransactionIdAndSegmentAccountId(Integer transId, Integer accountId);

    @Query(value = "select " +
            "MIN(SubLedger.id), " +
            "SubLedger.FK_accountNo, " +
            "MIN(slentity.name), " +
            "MIN(SegmentAccount.FK_accountId), " +
            "SUM(SubLedger.debit + SubLedger.credit) AS amount, " +
            "SUM(SubLedger.debit), " +
            "SUM(SubLedger.credit) " +
            "from SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN slentity ON SubLedger.FK_accountNo = slentity.accountNo " +
            "where SubLedger.FK_transactionId = :transId " +
            "AND SegmentAccount.FK_accountId = :accountId " +
            "GROUP BY SubLedger.FK_accountNo", nativeQuery = true)
    public List<Object[]> findByTransactionIdAndAccountId(@Param("transId") Integer transId, @Param("accountId") Integer accountId);

    @Query(value = "select " +
            "MIN(SubLedger.id), " +
            "SubLedger.FK_accountNo, " +
            "MIN(slentity.name), " +
            "MIN(SegmentAccount.FK_accountId), " +
            "SUM(SubLedger.debit + SubLedger.credit) AS amount " +
            "from SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN slentity ON SubLedger.FK_accountNo = slentity.accountNo " +
            "where SubLedger.FK_transactionId = :transId " +
            "AND SegmentAccount.FK_accountId = :accountId " +
            "AND debit != 0 " +
            "GROUP BY SubLedger.FK_accountNo", nativeQuery = true)
    List<Object[]> findByTransactionIdAndAccountIdAndDebit(@Param("transId") Integer transId, @Param("accountId") Integer accountId);

    @Query(value = "select " +
            "MIN(SubLedger.id), " +
            "SubLedger.FK_accountNo, " +
            "MIN(slentity.name), " +
            "MIN(SegmentAccount.FK_accountId), " +
            "SUM(SubLedger.debit + SubLedger.credit) AS amount " +
            "from SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN slentity ON SubLedger.FK_accountNo = slentity.accountNo " +
            "where SubLedger.FK_transactionId = :transId " +
            "AND SegmentAccount.FK_accountId = :accountId " +
            "AND credit != 0 " +
            "GROUP BY SubLedger.FK_accountNo", nativeQuery = true)
    List<Object[]> findByTransactionIdAndAccountIdAndCredit(@Param("transId") Integer transId, @Param("accountId") Integer accountId);

    @Query(value = "select " +
            "MIN(SubLedger.id), " +
            "SubLedger.FK_accountNo, " +
            "MIN(slentity.name), " +
            "MIN(SegmentAccount.FK_accountId), " +
            "SUM(SubLedger.debit + SubLedger.credit) AS amount " +
            "from SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN slentity ON SubLedger.FK_accountNo = slentity.accountNo " +
            "where SubLedger.FK_transactionId = :transId " +
            "GROUP BY SubLedger.FK_accountNo", nativeQuery = true)
    public List<Object[]> findByTransactionIdNative(@Param("transId") Integer transId);

    @Query(value = "SELECT " +
            "sl.id, " +
            "sl.FK_segmentAccountId, " +
            "sl.FK_generalLedgerLineId, " +
            "SUM(sl.debit) AS debit, " +
            "SUM(sl.credit) AS credit, " +
            "sl.FK_accountNo, " +
            "sl.FK_transactionId " +
            "FROM SubLedger sl " +
            "WHERE sl.FK_accountNo = :accountNo", nativeQuery = true)
    public SubLedger findByAccountNoSumDebitCredit(@Param("accountNo") Integer accountNo);

    @Query(value = "SELECT " +
            "SubLedger.id, " +
            "SubLedger.FK_accountNo, " +
            "COALESCE(SubLedger.debit) + COALESCE(SubLedger.credit) amount, " +
            "SegmentAccount.FK_accountId as accountId, " +
            "SegmentAccount.id as segmentAccountId " +
            "FROM SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "WHERE SubLedger.FK_transactionId = :transId " +
            "AND SubLedger.FK_accountNo = :accountNo", nativeQuery = true)
    public List<Object[]> findByTransactionIdAndAccountNo(@Param("transId") Integer transId, @Param("accountNo") Integer accountId);

    @Query(value = "SELECT SUM(SubLedger.debit) AS debit FROM SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN AssetDetail ON SegmentAccount.FK_accountId = AssetDetail.FK_assetAccountId " +
            "WHERE SubLedger.FK_accountNo = :assetAccountNo AND SubLedger.FK_transactionId = :voucherTransNo " +
            "AND AssetDetail.FK_assetId = :assetId AND SegmentAccount.FK_accountId = :accountId " +
            "GROUP BY SegmentAccount.FK_accountId LIMIT 1",
            nativeQuery = true)
    List<Object[]> findAssetAccountDebitEntry(@Param("assetAccountNo") Integer assetAccountNo,
                                              @Param("voucherTransNo") Integer voucherTransNo,
                                              @Param("assetId") Integer assetId,
                                              @Param("accountId") Integer accountId);

    @Query(value = "SELECT SUM(COALESCE(debit, 0)) as totalCost FROM SubLedger " +
            "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "WHERE FK_transactionId = :voucherTransNo AND SegmentAccount.FK_accountId = :accountId LIMIT 1",
            nativeQuery = true)
    List<Object[]> sumDebitByTransNoAndAccountId(@Param("voucherTransNo") Integer voucherTransNo,
                                              @Param("accountId") Integer accountId);
}
