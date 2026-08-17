package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MemorandumReceipt;
import com.noreco1.fireflyv2.model.StockTransfer;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/26/2020.
 */
public interface MemorandumReceiptRepo extends JpaRepository<MemorandumReceipt, Integer> {

    Page<MemorandumReceipt> findAllByDateBetweenOrderByDateAscCodeAsc(Date startDate, Date endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByCodeContainsAndDateBetweenOrderByDateAscCodeAsc(String code, Date startDate, Date endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(Integer employeeAccountNo, Date startDate, Date endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByCodeContainsAndEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(String code, Integer employeeAccountNo, Date startDate, Date endDate, Pageable pageable);

    @Query(value = "SELECT m.code FROM MemorandumReceipt m WHERE year(m.date) = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT * FROM MemorandumReceipt mr " +
            "INNER JOIN StockWithdrawal sw ON mr.FK_stockWithdrawalId = sw.id " +
            "WHERE (UPPER(sw.code) LIKE :query OR UPPER(sw.description) LIKE :query) " +
            "AND sw.id IN (SELECT FK_stockWithdrawalId " +
            "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
            "AND sw.FK_inventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM MemorandumReceipt mr " +
                    "INNER JOIN StockWithdrawal sw ON mr.FK_stockWithdrawalId = sw.id " +
                    "WHERE (UPPER(sw.code) LIKE :query OR UPPER(sw.description) LIKE :query) " +
                    "AND sw.id IN (SELECT FK_stockWithdrawalId " +
                    "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
                    "AND sw.FK_inventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<MemorandumReceipt> findAllForStockRelease(@Param("query") String query,
                                                   @Param("invLocId") Integer invLocId,
                                                   Pageable pageable);

    @Query(value = "SELECT * FROM MemorandumReceipt mr " +
            "INNER JOIN StockWithdrawal sw ON mr.FK_stockWithdrawalId = sw.id " +
            "WHERE sw.id IN (SELECT FK_stockWithdrawalId " +
            "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
            "AND sw.FK_inventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM MemorandumReceipt mr " +
                    "INNER JOIN StockWithdrawal sw ON mr.FK_stockWithdrawalId = sw.id " +
                    "WHERE sw.id IN (SELECT FK_stockWithdrawalId " +
                    "FROM StockWithdrawalDetail WHERE quantity > quantityReleased) " +
                    "AND sw.FK_inventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<MemorandumReceipt> findAllForStockRelease(@Param("invLocId") Integer invLocId, Pageable pageable);

    MemorandumReceipt findByTransactionId(Integer id);

    ArrayList<MemorandumReceipt> findAllByEmployeeAccountNoAndDocumentStatusId(Integer accountNo, Integer status);

    @Query(value = "SELECT * FROM MemorandumReceipt " +
            "INNER JOIN MemorandumReceiptDetail ON MemorandumReceipt.id = MemorandumReceiptDetail.FK_memorandumReceiptId " +
            "WHERE MemorandumReceiptDetail.quantity > ( " +
            "  SELECT IF(sum(returnedQuantity) IS NOT NULL, sum(returnedQuantity), 0) " +
            "  FROM ReturnMemorandumReceiptDetail " +
            "  JOIN ReturnMemorandumReceipt ON ReturnMemorandumReceipt.id = ReturnMemorandumReceiptDetail.FK_returnMemorandumReceiptId " +
            "  WHERE ReturnMemorandumReceiptDetail.FK_stockWithdrawalDetailId = MemorandumReceiptDetail.FK_stockWithdrawalDetailId " +
            "  AND ReturnMemorandumReceipt.FK_memorandumReceiptId = MemorandumReceipt.id " +
            ") " +
            "AND MemorandumReceipt.FK_employeeAccountNo = :accountNo " +
            "AND MemorandumReceipt.FK_documentStatusId = :status " +
            "GROUP BY MemorandumReceipt.code " +
            "ORDER BY MemorandumReceipt.code ", nativeQuery = true)
    ArrayList<MemorandumReceipt> findAllByEmployeeAccountNoNotInReturnMemorandumReceipt(@Param("accountNo") Integer accountNo, @Param("status") Integer status);

    ArrayList<MemorandumReceipt> findAllByDocumentStatusIdAndEmployeeAccountNoIsNull(Integer status);

    @Query(value = "SELECT * FROM MemorandumReceipt " +
            "INNER JOIN MemorandumReceiptDetail ON MemorandumReceipt.id = MemorandumReceiptDetail.FK_memorandumReceiptId " +
            "WHERE MemorandumReceiptDetail.quantity > ( " +
            "  SELECT IF(sum(returnedQuantity) IS NOT NULL, sum(returnedQuantity), 0) " +
            "  FROM ReturnMemorandumReceiptDetail " +
            "  JOIN ReturnMemorandumReceipt ON ReturnMemorandumReceipt.id = ReturnMemorandumReceiptDetail.FK_returnMemorandumReceiptId " +
            "  WHERE ReturnMemorandumReceiptDetail.FK_stockWithdrawalDetailId = MemorandumReceiptDetail.FK_stockWithdrawalDetailId " +
            "  AND ReturnMemorandumReceipt.FK_memorandumReceiptId = MemorandumReceipt.id " +
            ") " +
            "AND MemorandumReceipt.FK_documentStatusId = :status " +
            "GROUP BY MemorandumReceipt.code " +
            "ORDER BY MemorandumReceipt.code ", nativeQuery = true)
    ArrayList<MemorandumReceipt> findAllByOfficeNotInReturnMemorandumReceipt(@Param("status") Integer status);

}
