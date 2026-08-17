package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashTransDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PettyCashTransDetailRepo extends JpaRepository<PettyCashTransDetail, Integer> {
    @Query(value = "select " +
            "pctd.id, " +
            "pctd.FK_pettyCashTransId, " +
            "pctd.amount, " +
            "pctd.remarks, " +
            "pctd.balance " +
            "from PettyCashTransDetail pctd " +
            "where pctd.FK_pettyCashTransId = :pcvId",
            nativeQuery = true)
    List<PettyCashTransDetail> findByPCVId(@Param("pcvId") Integer pcvId);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "sum(pctd.amount), " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pct.id " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_pettyCashBatchId = :pcvBatchId " +
            "GROUP BY pct.id " +
            "ORDER BY pct.id  \n-- #pageable\n",
            countQuery="SELECT COUNT(*)" +
                    "FROM PettyCashTrans pct " +
                    "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
                    "WHERE pct.FK_pettyCashBatchId = :pcvBatchId " +
                    "GROUP BY pct.id ",
            nativeQuery = true)
    Page<Object[]> findByPCVBatchIPaged(@Param("pcvBatchId") Integer pcvBatchId, Pageable pageable);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "sum(pctd.amount), " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pct.id " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "GROUP BY pct.id " +
            "ORDER BY pct.id  \n-- #pageable\n",
            countQuery="SELECT COUNT(*)" +
                    "FROM PettyCashTrans pct " +
                    "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
                    "WHERE pct.FK_documentStatusId = :documentStatusId " +
                    "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
                    "GROUP BY pct.id " +
                    "ORDER BY pct.id",
            nativeQuery = true)
    Page<Object[]> findByPCVBatchIAndDocumentStatusPaged(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "sum(pctd.amount), " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pct.id " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "GROUP BY pct.id " +
            "ORDER BY pct.id  \n-- #pageable\n",
            countQuery = "SELECT COUNT(*)" +
                    "FROM PettyCashTrans pct " +
                    "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
                    "WHERE pct.FK_officeId = :officeId " +
                    "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
                    "GROUP BY pct.id " +
                    "ORDER BY pct.id",
            nativeQuery = true)
    Page<Object[]> findByPCVBatchIAndOfficeIdPaged(@Param("pcvBatchId") Integer pcvBatchId, @Param("officeId") Integer officeId, Pageable pageable);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "sum(pctd.amount), " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pct.id " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "GROUP BY pct.id " +
            "ORDER BY pct.id  \n-- #pageable\n",
            countQuery = "SELECT COUNT(*)" +
                    "FROM PettyCashTrans pct " +
                    "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
                    "WHERE pct.FK_officeId = :officeId " +
                    "AND pct.FK_documentStatusId = :documentStatusId " +
                    "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
                    "GROUP BY pct.id " +
                    "ORDER BY pct.id",
            nativeQuery = true)
    Page<Object[]> findByPCVBatchIAndDocumentStatusAndOfficeIdPaged(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId, Pageable pageable);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "pctd.amount, " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pctd.remarks, " +
            "pct.amount as totalAmount " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findByPCVBatchID(@Param("pcvBatchId") Integer pcvBatchId);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "pctd.amount, " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pctd.remarks, " +
            "pct.amount as totalAmount " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findByPCVBatchIDAndDocumentStatus(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "pctd.amount, " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pctd.remarks, " +
            "pct.amount as totalAmount " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findByPCVBatchIDAndOfficeId(@Param("pcvBatchId") Integer pcvBatchId, @Param("officeId") Integer officeId);

    @Query(value = "SELECT " +
            "pct.code, " +
            "pct.FK_accountNo, " +
            "pct.payee, " +
            "pctd.amount, " +
            "pct.voucherDate, " +
            "pct.FK_documentStatusId, " +
            "pct.FK_pettyCashBatchId, " +
            "pctd.remarks, " +
            "pct.amount as totalAmount " +
            "FROM PettyCashTrans pct " +
            "INNER JOIN PettyCashTransDetail pctd ON pct.id = pctd.FK_pettyCashTransId " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findByPCVBatchIDAndDocumentStatusAndOfficeId(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

    Long deleteByPettyCashTransId(Integer id);

    List<PettyCashTransDetail> findByPettyCashTransId(Integer id);

    @Query(value = "SELECT " +
            "MIN(pct.voucherDate) as startDate, " +
            "MAX(pct.voucherDate) as endDate " +
            "FROM PettyCashTrans pct " +
            "WHERE pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findDateRangeByPCVBatchID(@Param("pcvBatchId") Integer pcvBatchId);

    @Query(value = "SELECT " +
            "MIN(pct.voucherDate) as startDate, " +
            "MAX(pct.voucherDate) as endDate " +
            "FROM PettyCashTrans pct " +
            "WHERE pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findDateRangeByPCVBatchIDAndDocumentStatus(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "MIN(pct.voucherDate) as startDate, " +
            "MAX(pct.voucherDate) as endDate " +
            "FROM PettyCashTrans pct " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findDateRangeByPCVBatchIDAndOfficeId(@Param("pcvBatchId") Integer pcvBatchId, @Param("officeId") Integer officeId);

    @Query(value = "SELECT " +
            "MIN(pct.voucherDate) as startDate, " +
            "MAX(pct.voucherDate) as endDate " +
            "FROM PettyCashTrans pct " +
            "WHERE pct.FK_officeId = :officeId " +
            "AND pct.FK_documentStatusId = :documentStatusId " +
            "AND pct.FK_pettyCashBatchId = :pcvBatchId " +
            "ORDER BY pct.id",
            nativeQuery = true)
    List<Object[]> findDateRangeByPCVBatchIDAndDocumentStatusAndOfficeId(@Param("pcvBatchId") Integer pcvBatchId, @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

}
