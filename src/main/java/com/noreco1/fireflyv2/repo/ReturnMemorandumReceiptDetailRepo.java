package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceiptDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface ReturnMemorandumReceiptDetailRepo extends JpaRepository<ReturnMemorandumReceiptDetail, Integer> {

    @Transactional
    void deleteAllByReturnMemorandumReceiptId(Integer id);

    List<ReturnMemorandumReceiptDetail> findAllByReturnMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(Integer id);

    ArrayList<ReturnMemorandumReceiptDetail> findAllByReturnMemorandumReceiptIdAndUsableTrueOrderByStockWithdrawalDetailItemDescriptionAsc(Integer id);

    @Query(value = "select COALESCE(sum(rmrd.returnedQuantity), 0) as returned " +
            "from ReturnMemorandumReceiptDetail rmrd " +
            "JOIN ReturnMemorandumReceipt rmr ON rmr.id = rmrd.FK_returnMemorandumReceiptId " +
            "WHERE rmr.FK_memorandumReceiptId = :memorandumReceiptId AND rmrd.FK_stockWithdrawalDetailId = :stockWithdrawalDetailId ", nativeQuery = true)
    BigDecimal getReturnedQuantity(@Param("memorandumReceiptId") Integer memorandumReceiptId, @Param("stockWithdrawalDetailId") Integer stockWithdrawalDetailId);

    @Query(value = "SELECT " +
            "rmrd.* " +
            "FROM ReturnMemorandumReceiptDetail rmrd " +
            "INNER JOIN ReturnMemorandumReceipt rmr ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
            "WHERE rmrd.returnedQuantity > (SELECT IF(sum(mrd.reassignedQuantity) IS NOT NULL, sum(mrd.reassignedQuantity), 0) FROM MemorandumReceiptDetail mrd WHERE mrd.FK_stockWithdrawalDetailId = rmrd.FK_stockWithdrawalDetailId) " +
            "AND IF(LENGTH(:query) > 0, (rmr.code LIKE CONCAT('%', :query, '%')), 1) " +
            "ORDER BY rmr.code  \n#pageable\n",
            countQuery = "SELECT " +
                    "COUNT(*) " +
                    "FROM ReturnMemorandumReceiptDetail rmrd " +
                    "INNER JOIN ReturnMemorandumReceipt rmr ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
                    "WHERE rmrd.returnedQuantity > (SELECT IF(sum(mrd.reassignedQuantity) IS NOT NULL, sum(mrd.reassignedQuantity), 0) FROM MemorandumReceiptDetail mrd WHERE mrd.FK_stockWithdrawalDetailId = rmrd.FK_stockWithdrawalDetailId) " +
                    "AND IF(LENGTH(:query) > 0, (rmr.code LIKE CONCAT('%', :query, '%')), 1) " +
                    "ORDER BY rmr.code ",
            nativeQuery = true)
    Page<ReturnMemorandumReceiptDetail> findAllForMaterialSalvageTicket(@Param("query") String query, Pageable paging);
}
