package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MemorandumReceiptDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/26/2020.
 */
public interface MemorandumReceiptDetailRepo extends JpaRepository<MemorandumReceiptDetail, Integer> {

    Long deleteAllByMemorandumReceiptId(int memorandumReceiptId);
    List<MemorandumReceiptDetail> findAllByMemorandumReceiptId(int memorandumReceiptId);

    @Query(value = "SELECT mrd.*, i.description FROM MemorandumReceiptDetail mrd " +
            "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
            "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
            "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
            "WHERE mrd.FK_memorandumReceiptId = :memorandumReceiptId " +
            "ORDER BY i.description ", nativeQuery = true)
    List<MemorandumReceiptDetail> findAllByMemorandumReceiptIdOrderByItemDescription(@Param("memorandumReceiptId") Integer memorandumReceiptId);

    List<MemorandumReceiptDetail> findAllByMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(Integer memorandumReceiptId);

    @Query(value = "select strd.itemStockBalance as balance, sum(mrd.quantity) as assigned from MemorandumReceiptDetail mrd " +
            "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
            "WHERE strd.id = :id ", nativeQuery = true)
    List<Object[]> getStockTransactionBalance(@Param("id") Integer id);

    @Query(value = "select id, COALESCE(sum(mrd.quantity), 0) as assigned from MemorandumReceiptDetail mrd " +
            "WHERE mrd.FK_stockWithdrawalDetailId = :id ", nativeQuery = true)
    List<Object[]> getStockWithdrawalBalance(@Param("id") Integer id);

    @Query(value = "SELECT mrd.* FROM MemorandumReceiptDetail mrd " +
            "LEFT JOIN MemorandumReceipt mr ON mrd.FK_memorandumReceiptId = mr.id " +
            "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
            "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
            "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
            "WHERE mr.FK_employeeAccountNo = :accountNo " +
            "AND mrd.quantity > 0 " +
            "AND mrd.quantity > mrd.returnedQuantity " +
            "AND IF(LENGTH(:query) > 0, (i.code LIKE CONCAT('%', :query, '%') OR i.description LIKE CONCAT('%', :query, '%')), 1) " +
            "ORDER BY i.description  \n#pageable\n",
            countQuery = "SELECT count(*) FROM MemorandumReceiptDetail mrd " +
                    "LEFT JOIN MemorandumReceipt mr ON mrd.FK_memorandumReceiptId = mr.id " +
                    "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
                    "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
                    "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
                    "WHERE mr.FK_employeeAccountNo = :accountNo " +
                    "AND mrd.quantity > 0 " +
                    "AND mrd.quantity > mrd.returnedQuantity " +
                    "AND IF(LENGTH(:query) > 0, (i.code LIKE CONCAT('%', :query, '%') OR i.description LIKE CONCAT('%', :query, '%')), 1) ",
            nativeQuery = true)
    Page<MemorandumReceiptDetail> findAllForMaterialSalvageTicket(@Param("query") String query, @Param("accountNo") Integer accountNo, Pageable paging);

    @Query(value = "SELECT SUM(itd.quantity) AS totalReturned FROM ItemTransactionDetail itd " +
            "LEFT JOIN ReturnMemorandumReceiptDetail mrd ON itd.FK_returnMemorandumReceiptDetailId = mrd.id " +
            "WHERE mrd.id = :id ", nativeQuery = true)
    Object totalReturned(@Param("id") Integer id);

    @Query(value = "SELECT " +
            "mr.id, " +
            "mr.code AS `mrcode`, " +
            "mr.date AS `mrdate`, " +
            "rr.code AS `rrCode`, " +
            "COALESCE(returnedMR.mrCode, '') AS `rmrcode`, " +
            "returnedMR.mrDate AS `rmrdate`, " +
            "i.code AS `itemCode`, " +
            "i.description AS `itemDescription`, " +
            "rrd.unitPrice AS `rrUnitPrice`, " +
            "COALESCE(mrd.quantity, 0) AS `quantityAssigned`, " +
            "COALESCE(returnedMR.`totalReturned`, 0) AS `quantityReturned`, " +
            "COALESCE(mrd.quantity, 0) - COALESCE(returnedMR.`totalReturned`, 0) AS `balance` " +
            "FROM MemorandumReceipt mr " +
            "JOIN MemorandumReceiptDetail mrd ON mr.id = mrd.FK_memorandumReceiptId " +
            "JOIN StockWithdrawalDetail swd ON swd.id = mrd.FK_stockWithdrawalDetailId " +
            "JOIN StockWithdrawal sw ON sw.id = swd.FK_stockWithdrawalId " +
            "JOIN StockRelease sr ON sr.FK_documentTransactionId = sw.FK_transactionId " +
            "JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "JOIN StockTransactionDetail stdetail ON stdetail.FK_stockTransactionId = st.id " +
            "JOIN ItemStockDetail isd ON isd.id = stdetail.FK_itemStockDetailId " +
            "JOIN ReceivingReportDetail rrd ON rrd.id = isd.FK_rrDetailId " +
            "JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
            "JOIN Item i ON i.id = swd.FK_itemId  " +
            "LEFT JOIN ( " +
            "  SELECT  " +
            "  rmr.code AS mrCode, " +
            "  rmr.date AS mrDate, " +
            "  rmr.FK_memorandumReceiptId AS mrId, " +
            "  rmrd.FK_stockWithdrawalDetailId AS stockWithdrawalDetailId, " +
            "  SUM(rmrd.returnedQuantity) AS `totalReturned` " +
            "  FROM ReturnMemorandumReceipt rmr " +
            "  JOIN ReturnMemorandumReceiptDetail rmrd ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
            "  GROUP BY rmrd.FK_returnMemorandumReceiptId, rmrd.FK_stockWithdrawalDetailId " +
            ") AS returnedMR ON returnedMR.mrId = mr.id AND returnedMR.stockWithdrawalDetailId = mrd.FK_stockWithdrawalDetailId " +
            "WHERE mr.FK_employeeAccountNo = :acctNo " +
            "GROUP BY mrd.id " +
            "ORDER BY mr.date, mr.code, i.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "SELECT " +
                    "mr.id, " +
                    "mr.code AS `mrcode`, " +
                    "mr.date AS `mrdate`, " +
                    "rr.code AS `rrCode`, " +
                    "COALESCE(returnedMR.mrCode, '') AS `rmrcode`, " +
                    "returnedMR.mrDate AS `rmrdate`, " +
                    "i.code AS `itemCode`, " +
                    "i.description AS `itemDescription`, " +
                    "rrd.unitPrice AS `rrUnitPrice`, " +
                    "COALESCE(mrd.quantity, 0) AS `quantityAssigned`, " +
                    "COALESCE(returnedMR.`totalReturned`, 0) AS `quantityReturned`, " +
                    "COALESCE(mrd.quantity, 0) - COALESCE(returnedMR.`totalReturned`, 0) AS `balance` " +
                    "FROM MemorandumReceipt mr " +
                    "JOIN MemorandumReceiptDetail mrd ON mr.id = mrd.FK_memorandumReceiptId " +
                    "JOIN StockWithdrawalDetail swd ON swd.id = mrd.FK_stockWithdrawalDetailId " +
                    "JOIN StockWithdrawal sw ON sw.id = swd.FK_stockWithdrawalId " +
                    "JOIN StockRelease sr ON sr.FK_documentTransactionId = sw.FK_transactionId " +
                    "JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
                    "JOIN StockTransactionDetail stdetail ON stdetail.FK_stockTransactionId = st.id " +
                    "JOIN ItemStockDetail isd ON isd.id = stdetail.FK_itemStockDetailId " +
                    "JOIN ReceivingReportDetail rrd ON rrd.id = isd.FK_rrDetailId " +
                    "JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
                    "JOIN Item i ON i.id = swd.FK_itemId  " +
                    "LEFT JOIN ( " +
                    "  SELECT  " +
                    "  rmr.code AS mrCode, " +
                    "  rmr.date AS mrDate, " +
                    "  rmr.FK_memorandumReceiptId AS mrId, " +
                    "  rmrd.FK_stockWithdrawalDetailId AS stockWithdrawalDetailId, " +
                    "  SUM(rmrd.returnedQuantity) AS `totalReturned` " +
                    "  FROM ReturnMemorandumReceipt rmr " +
                    "  JOIN ReturnMemorandumReceiptDetail rmrd ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
                    "  GROUP BY rmrd.FK_returnMemorandumReceiptId, rmrd.FK_stockWithdrawalDetailId " +
                    ") AS returnedMR ON returnedMR.mrId = mr.id AND returnedMR.stockWithdrawalDetailId = mrd.FK_stockWithdrawalDetailId " +
                    "WHERE mr.FK_employeeAccountNo = :acctNo " +
                    "GROUP BY mrd.id " +
                    ") AS documents ",
            nativeQuery = true)
    Page<Object[]> findAllForMRTELedgerPaged(@Param("acctNo")Integer acctNo, Pageable paging);

    @Query(value = "SELECT * FROM MemorandumReceiptDetail mrd " +
            "LEFT JOIN MemorandumReceipt mr ON mrd.FK_memorandumReceiptId = mr.id " +
            "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
            "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
            "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
            "WHERE mr.FK_employeeAccountNo = :acctNo " +
            "AND i.id = :itemId " +
            "ORDER BY mr.date, mr.code  \n#pageable\n",
            countQuery = "SELECT count(*) FROM MemorandumReceiptDetail mrd " +
                    "LEFT JOIN MemorandumReceipt mr ON mrd.FK_memorandumReceiptId = mr.id " +
                    "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
                    "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
                    "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
                    "WHERE mr.FK_employeeAccountNo = :acctNo " +
                    "AND i.id = :itemId ",
            nativeQuery = true)
    Page<MemorandumReceiptDetail> findAllForMRTELedgerByItemPaged(@Param("acctNo")Integer acctNo, @Param("itemId")Integer itemId, Pageable paging);

    @Query(value = "SELECT " +
            "mr.id, " +
            "mr.code AS `mrcode`, " +
            "mr.date AS `mrdate`, " +
            "rr.code AS `rrCode`, " +
            "COALESCE(returnedMR.mrCode, '') AS `rmrcode`, " +
            "returnedMR.mrDate AS `rmrdate`, " +
            "i.code AS `itemCode`, " +
            "i.description AS `itemDescription`, " +
            "rrd.unitPrice AS `rrUnitPrice`, " +
            "COALESCE(mrd.quantity, 0) AS `quantityAssigned`, " +
            "COALESCE(returnedMR.`totalReturned`, 0) AS `quantityReturned`, " +
            "COALESCE(mrd.quantity, 0) - COALESCE(returnedMR.`totalReturned`, 0) AS `balance` " +
            "FROM MemorandumReceipt mr " +
            "JOIN MemorandumReceiptDetail mrd ON mr.id = mrd.FK_memorandumReceiptId " +
            "JOIN StockWithdrawalDetail swd ON swd.id = mrd.FK_stockWithdrawalDetailId " +
            "JOIN StockWithdrawal sw ON sw.id = swd.FK_stockWithdrawalId " +
            "JOIN StockRelease sr ON sr.FK_documentTransactionId = sw.FK_transactionId " +
            "JOIN StockTransaction st ON st.FK_transactionId = sr.FK_transactionId " +
            "JOIN StockTransactionDetail stdetail ON stdetail.FK_stockTransactionId = st.id " +
            "JOIN ItemStockDetail isd ON isd.id = stdetail.FK_itemStockDetailId " +
            "JOIN ReceivingReportDetail rrd ON rrd.id = isd.FK_rrDetailId " +
            "JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
            "JOIN Item i ON i.id = swd.FK_itemId  " +
            "LEFT JOIN ( " +
            "  SELECT  " +
            "  rmr.code AS mrCode, " +
            "  rmr.date AS mrDate, " +
            "  rmr.FK_memorandumReceiptId AS mrId, " +
            "  rmrd.FK_stockWithdrawalDetailId AS stockWithdrawalDetailId, " +
            "  SUM(rmrd.returnedQuantity) AS `totalReturned` " +
            "  FROM ReturnMemorandumReceipt rmr " +
            "  JOIN ReturnMemorandumReceiptDetail rmrd ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
            "  GROUP BY rmrd.FK_returnMemorandumReceiptId, rmrd.FK_stockWithdrawalDetailId " +
            ") AS returnedMR ON returnedMR.mrId = mr.id AND returnedMR.stockWithdrawalDetailId = mrd.FK_stockWithdrawalDetailId " +
            "WHERE mr.FK_employeeAccountNo = :acctNo " +
            "GROUP BY mrd.id " +
            "ORDER BY mr.date, mr.code, i.description ",
            nativeQuery = true)
    List<Object[]> findAllForMRTELedger(@Param("acctNo")Integer acctNo);

    @Query(value = "SELECT * FROM MemorandumReceiptDetail mrd " +
            "LEFT JOIN MemorandumReceipt mr ON mrd.FK_memorandumReceiptId = mr.id " +
            "LEFT JOIN StockTransactionDetail strd ON mrd.FK_stockTransactionDetailId = strd.id " +
            "LEFT JOIN ItemStock ist ON ist.id = strd.FK_itemStockId " +
            "LEFT JOIN Item i ON i.id = ist.FK_itemId " +
            "WHERE mr.FK_employeeAccountNo = :acctNo " +
            "AND i.id = :itemId " +
            "ORDER BY mr.date, mr.code ",
            nativeQuery = true)
    List<MemorandumReceiptDetail> findAllForMRTELedgerByItem(@Param("acctNo")Integer acctNo, @Param("itemId")Integer itemId);

    @Query(value = "SELECT * FROM MemorandumReceiptDetail " +
            "WHERE MemorandumReceiptDetail.quantity > ( " +
            "  SELECT IF(sum(returnedQuantity) IS NOT NULL, sum(returnedQuantity), 0) " +
            "  FROM ReturnMemorandumReceiptDetail " +
            "  JOIN ReturnMemorandumReceipt ON ReturnMemorandumReceipt.id = ReturnMemorandumReceiptDetail.FK_returnMemorandumReceiptId " +
            "  WHERE ReturnMemorandumReceiptDetail.FK_stockWithdrawalDetailId = MemorandumReceiptDetail.FK_stockWithdrawalDetailId " +
            "  AND ReturnMemorandumReceipt.FK_memorandumReceiptId = MemorandumReceiptDetail.FK_memorandumReceiptId " +
            ") " +
            "AND MemorandumReceiptDetail.FK_memorandumReceiptId = :memorandumReceiptId ", nativeQuery = true)
    List<MemorandumReceiptDetail> findAllByMemorandumReceiptDetailNotInReturnDetailOrderByItemDescription(@Param("memorandumReceiptId") Integer memorandumReceiptId);

    @Query(value = "select COALESCE(sum(mrd.reassignedQuantity), 0) as returned from MemorandumReceiptDetail mrd " +
            "WHERE mrd.FK_stockWithdrawalDetailId = :id ", nativeQuery = true)
    BigDecimal getReassignedQuantity(@Param("id") Integer id);
}
