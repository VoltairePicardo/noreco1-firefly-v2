package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashTrans;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PettyCashTransRepo extends JpaRepository<PettyCashTrans, Integer> {
    @Query(value = "SELECT e.code FROM PettyCashTrans e WHERE year(e.pettyCashDate) = :year  AND code LIKE '%PCV%'  AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestPcvCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    PettyCashTrans findOneByTransactionId(Integer transId);

    List<PettyCashTrans> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "select " +
            "la.* " +
            "from PettyCashTrans la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id  " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` releaseUser on la.FK_releasedByUserId = releaseUser.id " +
            "WHERE approveUser.id = :userId OR createUser.id = :userId  " +
            "OR checkUser.id = :userId OR releaseUser.id = :userId",
            nativeQuery = true)
    List<PettyCashTrans> findAllByAllowedUsers(@Param("userId") Integer userId);

    @Query(value = "SELECT SUM(amount) FROM PettyCashTrans " +
                    "WHERE FK_documentStatusId = 7 AND PettyCashTrans.voucherDate < :date LIMIT 1",  // 7 - approved
                    nativeQuery = true)
    List<Object[]> findTotalBeforeDate(@Param("date") String date);

    @Query(value = "SELECT  " +
            "COALESCE(SUM(ptd.amount), 0) " +
            "FROM PettyCashTrans p " +
            "INNER JOIN PettyCashTransDetail ptd ON ptd.FK_pettyCashTransId = p.id " +
            "WHERE p.FK_documentStatusId NOT IN(26, 25, 58)", nativeQuery = true)
    BigDecimal findSumOtherAmounts();

    @Query(value = "SELECT * " +
            "FROM PettyCashTrans p " +
            "WHERE p.FK_documentStatusId = :documentStatusId " +
            "AND p.request = :request " +
            "AND p.id NOT IN (SELECT pcl.FK_pettyCashTransId FROM PettyCashLiquidation pcl WHERE " +
            "pcl.FK_documentStatusId NOT IN (8, 26)) " +
            "AND (p.code LIKE :filter OR p.payee LIKE :filter) " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PettyCashTrans p " +
                    "WHERE p.FK_documentStatusId = :documentStatusId " +
                    "AND p.request = :request " +
                    "AND p.id NOT IN (SELECT pcl.FK_pettyCashTransId FROM PettyCashLiquidation pcl WHERE " +
                    "pcl.FK_documentStatusId NOT IN (8, 26)) " +
                    "AND (p.code LIKE :filter OR p.payee LIKE :filter)",
            nativeQuery = true)
    Page<PettyCashTrans> findByStatusAndFilterAndReleaseType(@Param("documentStatusId") Integer documentStatusId,
                                                             @Param("filter") String filter,
                                                             @Param("request") String request, Pageable pageable);
}
