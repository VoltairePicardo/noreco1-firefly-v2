package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvance;
import com.noreco1.fireflyv2.model.CashAdvanceLiquidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by TSI on 5/27/2023.
 */
public interface CashAdvanceLiquidationRepo extends JpaRepository<CashAdvanceLiquidation, Integer> {

    List<CashAdvanceLiquidation> findByDocumentStatusId(Integer statusId);

    CashAdvanceLiquidation findFirstByOrderByIdAsc();

    CashAdvanceLiquidation findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "e.code " +
            "FROM CashAdvanceLiquidation e " +
            "WHERE year(e.createdAt) = :year  AND code LIKE '%CAL%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCaCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    List<CashAdvanceLiquidation> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);

    @Query(value = "select " +
            "cal.* " +
            "from CashAdvanceLiquidation cal  " +
            "LEFT JOIN User approveUser on cal.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN User createUser on cal.FK_createdByUserId = createUser.id " +
            "LEFT JOIN User recommendUser on cal.FK_recommendedByUserId = recommendUser.id " +
            "WHERE approveUser.id = :userId OR createUser.id = :userId OR recommendUser.id = :userId ",
            nativeQuery = true)
    List<CashAdvanceLiquidation> findAllByAllowedUsers(@Param("userId") Integer userId);

    @Query(value = "select " +
            "cal.* " +
            "from CashAdvanceLiquidation cal  " +
            "LEFT JOIN User approveUser on cal.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN User createUser on cal.FK_createdByUserId = createUser.id " +
            "LEFT JOIN User recommendUser on cal.FK_recommendedByUserId = recommendUser.id " +
            "WHERE cal.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) " +
            "AND approveUser.id = :userId OR createUser.id = :userId OR recommendUser.id = :userId ",
            nativeQuery = true)
    List<CashAdvanceLiquidation> findAllByDateAndStatusAndAllowedUsers(@Param("userId") Integer userId,
                                                                       @Param("from") Date from,
                                                                       @Param("to") Date to,
                                                                       @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "select " +
            "cal.* " +
            "from CashAdvanceLiquidation cal  " +
            "LEFT JOIN User approveUser on cal.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN User createUser on cal.FK_createdByUserId = createUser.id " +
            "LEFT JOIN User recommendUser on cal.FK_recommendedByUserId = recommendUser.id " +
            "WHERE cal.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusIds) " +
            "AND approveUser.id = :userId OR createUser.id = :userId OR recommendUser.id = :userId ",
            nativeQuery = true)
    List<CashAdvanceLiquidation> findAllPendingByDateAndAllowedUsers(@Param("userId") Integer userId,
                                                                       @Param("from") Date from,
                                                                       @Param("to") Date to,
                                                                      @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "select " +
            "c.*" +
            "from CashAdvanceLiquidation c " +
            "LEFT JOIN `User` createUser on c.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` recommendUser on c.FK_recommendedByUserId = recommendUser.id " +
            "LEFT JOIN `User` approveUser on c.FK_approvedByUserId = approveUser.id " +
            "WHERE c.FK_documentStatusId = :statusId " +
            "AND (c.voucherDate between :from and :to) " +
            "AND FK_officeId = :officeId " +
            "AND (approveUser.id = :userId " +
            "OR createUser.id = :userId " +
            "OR recommendUser.id = :userId )",
            nativeQuery = true)
    List<CashAdvanceLiquidation> findByDateRangeAndStatusIdAndOfficeId(
            @Param("userId") Integer userId,
            @Param("statusId") Integer statusId,
            @Param("from") Date from,
            @Param("to") Date to, @Param("officeId") Integer officeId);

    @Query(value = "select " +
            "c.* " +
            "from CashAdvanceLiquidation c " +
            "LEFT JOIN `User` createUser on c.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` approveUser on c.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` recommendUser on c.FK_recommendedByUserId = recommendUser.id " +
            "WHERE c.FK_documentStatusId not in (:documentStatusIds) " +
            "AND (c.voucherDate between :from and :to) " +
            "AND FK_officeId = :officeId " +
            "AND (approveUser.id = :userId " +
            "OR createUser.id = :userId " +
            "OR recommendUser.id = :userId)",
            nativeQuery = true)
    List<CashAdvanceLiquidation> findByDateRangeAndNotApprovedOrDeniedAndOfficeId(
            @Param("userId") Integer userId,
            @Param("documentStatusIds") Collection<Integer> documentStatusIds,
            @Param("from") Date from,
            @Param("to") Date to, @Param("officeId") Integer officeId);

    @Query(value = "SELECT * FROM CashAdvanceLiquidation cal " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE cal.FK_documentStatusId = 7 AND jv.id IS null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM CashAdvanceLiquidation cal " +
                    "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
                    "WHERE cal.FK_documentStatusId = 7 AND jv.id IS null",
            nativeQuery = true)
    Page<CashAdvanceLiquidation> findAllForJV(Pageable pageable);

    @Query(value = "SELECT * FROM CashAdvanceLiquidation cal " +
            "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
            "WHERE cal.code like :query AND cal.FK_documentStatusId = 7 AND jv.id IS null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM CashAdvanceLiquidation cal " +
                    "LEFT JOIN JournalVoucher jv ON cal.id = jv.FK_cashAdvanceLiquidationId " +
                    "WHERE cal.code like :query AND cal.FK_documentStatusId = 7 AND jv.id IS null",
            nativeQuery = true)
    Page<CashAdvanceLiquidation> findAllForJVByQuery(@Param("query") String query, Pageable pageable);

}
