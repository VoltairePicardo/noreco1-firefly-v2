package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface ReturnMemorandumReceiptRepo extends JpaRepository<ReturnMemorandumReceipt, Integer> {

    Page<ReturnMemorandumReceipt> findAllByDateBetweenOrderByDateAscCodeAsc(Date startDate, Date endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByCodeContainsAndDateBetweenOrderByDateAscCodeAsc(String code, Date startDate, Date endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByMemorandumReceiptEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(Integer employeeAccountNo, Date startDate, Date endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByCodeContainsAndMemorandumReceiptEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(String code, Integer employeeAccountNo, Date startDate, Date endDate, Pageable pageable);

    ReturnMemorandumReceipt findByTransactionId(Integer id);

    @Query(value = "SELECT m.code FROM ReturnMemorandumReceipt m WHERE year(m.date) = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT * FROM ReturnMemorandumReceipt rmr " +
            "INNER JOIN ReturnMemorandumReceiptDetail rmrd ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
            "WHERE rmr.FK_documentStatusId = 7 " +
            "AND IF(LENGTH(:query) > 0, (rmr.code LIKE CONCAT('%', :query, '%') OR rmr.remarks LIKE CONCAT('%', :query, '%')), 1) " +
            "AND rmrd.returnedQuantity > (SELECT IF(sum(mrd.reassignedQuantity) IS NOT NULL, sum(mrd.reassignedQuantity), 0) FROM MemorandumReceiptDetail mrd WHERE mrd.FK_stockWithdrawalDetailId = rmrd.FK_stockWithdrawalDetailId) " +
            "GROUP BY rmr.id  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ReturnMemorandumReceipt rmr " +
                    "INNER JOIN ReturnMemorandumReceiptDetail rmrd ON rmrd.FK_returnMemorandumReceiptId = rmr.id " +
                    "WHERE rmr.FK_documentStatusId = 7 " +
                    "AND IF(LENGTH(:query) > 0, (rmr.code LIKE CONCAT('%', :query, '%') OR rmr.remarks LIKE CONCAT('%', :query, '%')), 1) " +
                    "AND rmrd.returnedQuantity > (SELECT IF(sum(mrd.reassignedQuantity) IS NOT NULL, sum(mrd.reassignedQuantity), 0) FROM MemorandumReceiptDetail mrd WHERE mrd.FK_stockWithdrawalDetailId = rmrd.FK_stockWithdrawalDetailId) " +
                    "GROUP BY rmr.id ",
            nativeQuery = true)
    Page<ReturnMemorandumReceipt> findAllForMemorandumReceipt(@Param("query") String query, Pageable paging);

}
