package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CashAdvanceRepo extends JpaRepository<CashAdvance, Integer> {
    @Query(value = "SELECT " +
            "e.code " +
            "FROM CashAdvance e " +
            "WHERE year(e.cashAdvanceDate) = :year  AND code LIKE '%CA%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCaCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    CashAdvance findOneByTransactionId(Integer transId);

    List<CashAdvance> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    CashAdvance findFirstByOrderByIdAsc();

    List<CashAdvance> findByDocumentStatusId(Integer statusId);

    List<CashAdvance> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);

    @Query(value = "select " +
            "ca.* " +
            "from CashAdvance ca " +
            "where ca.FK_documentStatusId = :statusId " +
            "and ca.voucherDate between :from and :to " +
            "and ca.id in(select " +
            "c.id " +
            "from CashAdvance c " +
            "LEFT JOIN `User` createUser on c.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` recommendUser on c.FK_recommendedByUserId = recommendUser.id " +
            "LEFT JOIN `User` approveUser on c.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` checkUser on c.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` auditUser on c.FK_auditedByUserId = auditUser.id " +
            "WHERE (FK_officeId = :officeId AND approveUser.id = :userId) " +
            "OR createUser.id = :userId " +
            "OR recommendUser.id = :userId " +
            "OR checkUser.id = :userId " +
            "OR auditUser.id = :userId)",
            nativeQuery = true)
    List<CashAdvance> findByDateRangeAndStatusIdAndOfficeId(
            @Param("userId") Integer userId,
            @Param("statusId") Integer statusId,
            @Param("from") Date from,
            @Param("to") Date to, @Param("officeId") Integer officeId);

    @Query(value = "select " +
            "ca.* " +
            "from CashAdvance ca " +
            "where ca.FK_documentStatusId not in (:documentStatusIds) " +
            "and ca.voucherDate between :from and :to " +
            "and ca.id in(select " +
            "c.id " +
            "from CashAdvance c " +
            "LEFT JOIN `User` createUser on c.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` approveUser on c.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` notedBy on c.FK_notedByUserId = notedBy.id " +
            "WHERE (FK_officeId = :officeId AND approveUser.id = :userId) " +
            "OR createUser.id = :userId " +
            "OR notedBy.id = :userId)",
            nativeQuery = true)
    List<CashAdvance> findByDateRangeAndNotApprovedOrDeniedAndOfficeId(
            @Param("userId") Integer userId,
            @Param("documentStatusIds") Collection<Integer> documentStatusIds,
            @Param("from") Date from,
            @Param("to") Date to, @Param("officeId") Integer officeId);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE id NOT IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance) " +
            "AND FK_documentStatusId = 7  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE id NOT IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance) " +
                    "AND FK_documentStatusId = 7",
            nativeQuery = true)
    Page<CashAdvance> findAllForCV(Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE id NOT IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance) " +
            "AND FK_documentStatusId = 7 " +
            "AND code LIKE :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE id NOT IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance) " +
                    "AND FK_documentStatusId = 7 " +
                    "AND code LIKE :query",
            nativeQuery = true)
    Page<CashAdvance> findAllForCVByQuery(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE FK_documentStatusId = 7  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE FK_documentStatusId = 7",
            nativeQuery = true)
    Page<CashAdvance> findAllForPO(Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE FK_documentStatusId = 7 " +
            "AND code LIKE :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE FK_documentStatusId = 7 " +
                    "AND code LIKE :query",
            nativeQuery = true)
    Page<CashAdvance> findAllForPOByQuery(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "WHERE ca.FK_documentStatusId = :documentStatusId " +
            "AND ca.id " +
            "NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherCashAdvance.FK_cashAdvanceId  " +
            "  FROM CheckVoucherCashAdvance " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherCashAdvance.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance ca " +
                    "WHERE ca.FK_documentStatusId = :documentStatusId " +
                    "AND ca.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherCashAdvance.FK_cashAdvanceId  " +
                    "  FROM CheckVoucherCashAdvance " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherCashAdvance.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") ",
            nativeQuery = true)
    Page<CashAdvance> findAllByDocumentStatusForCv(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "WHERE ca.FK_documentStatusId = :documentStatusId " +
            "AND ca.code LIKE :query " +
            "AND ca.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherCashAdvance.FK_cashAdvanceId  " +
            "  FROM CheckVoucherCashAdvance " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherCashAdvance.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance ca " +
                    "WHERE ca.FK_documentStatusId = :documentStatusId " +
                    "AND ca.code LIKE :query " +
                    "AND ca.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherCashAdvance.FK_cashAdvanceId  " +
                    "  FROM CheckVoucherCashAdvance " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherCashAdvance.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") ",
            nativeQuery = true)
    Page<CashAdvance> findAllByQueryAndDocumentStatusForCv(@Param("query") String query,
                                                           @Param("documentStatusId") Integer documentStatusId,
                                                           Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE id IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance cvca JOIN CheckVoucher cv ON cvca.FK_checkVoucherId = cv.id WHERE CV.FK_documentStatusId = 7) " +
            "AND FK_documentStatusId = 7  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE id IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance cvca JOIN CheckVoucher cv ON cvca.FK_checkVoucherId = cv.id WHERE CV.FK_documentStatusId = 7) " +
                    "AND FK_documentStatusId = 7",
            nativeQuery = true)
    Page<CashAdvance> findAllForLiquidation(Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance " +
            "WHERE id IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance cvca JOIN CheckVoucher cv ON cvca.FK_checkVoucherId = cv.id WHERE CV.FK_documentStatusId = 7) " +
            "AND FK_documentStatusId = 7 " +
            "AND code LIKE :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CashAdvance " +
                    "WHERE id IN (SELECT FK_cashAdvanceId FROM CheckVoucherCashAdvance cvca JOIN CheckVoucher cv ON cvca.FK_checkVoucherId = cv.id WHERE CV.FK_documentStatusId = 7) " +
                    "AND FK_documentStatusId = 7 " +
                    "AND code LIKE :query",
            nativeQuery = true)
    Page<CashAdvance> findAllForLiquidationByQuery(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "left JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE (cal.id IS NULL OR jv.id IS NULL OR jv.FK_documentStatusId != 7) " +
            "ORDER BY ca.code",
            nativeQuery = true)
    List<CashAdvance> findAllUnliquidated();

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "left JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE (cal.id IS NULL OR jv.id IS NULL OR jv.FK_documentStatusId != 7) " +
            "AND CURRENT_DATE() > DATE_ADD(ca.periodCoveredTo, INTERVAL 5 DAY) " +
            "ORDER BY ca.code",
            nativeQuery = true)
    List<CashAdvance> findAllUnliquidatedOverdue();

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "left JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE (cal.id IS NULL OR jv.id IS NULL OR jv.FK_documentStatusId != 7) " +
            "AND CURRENT_DATE() <= DATE_ADD(ca.periodCoveredTo, INTERVAL 5 DAY) " +
            "ORDER BY ca.code ",
            nativeQuery = true)
    List<CashAdvance> findAllUnliquidatedNotOverdue();

    @Query(value = "SELECT * FROM CashAdvance ca " +
            "left JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE (cal.id IS NULL OR jv.id IS NULL OR jv.FK_documentStatusId != 7) " +
            "AND ca.FK_employeeId = :employeeId " +
            "ORDER BY ca.code",
            nativeQuery = true)
    List<CashAdvance> findAllUnliquidatedByEmployee(@Param("employeeId") Integer employeeId);

}
