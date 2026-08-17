package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetLineItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BudgetLineItemRepo extends JpaRepository<BudgetLineItem, Integer> {

    Page<BudgetLineItem> findAllByDivisionIdOrderByYearAsc(Integer userDivisionId, Pageable pageable);

    BudgetLineItem findOneByTransactionId(Integer id);

    BudgetLineItem findFirstByOrderByIdAsc();

//    Page<BudgetLineItem> findAllByYear(Integer year, Pageable pageable);
//    Page<BudgetLineItem> findAllByYearAndDivisionId(Integer year, Integer division, Pageable pageable);

    @Query(value = "SELECT bli.* FROM BudgetLineItem bli " +
            "LEFT JOIN BudgetLineItemDetail blid ON blid.FK_budgetLineItemId = bli.id " +
            "WHERE bli.`year` = :year " +
            "AND (bli.FK_createdByUserId = :userId OR bli.FK_checkedByUserId = :userId OR bli.FK_approvedByUserId = :userId) " +
            "GROUP BY bli.id " +
            "ORDER BY bli.`year` " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "  SELECT bli.* FROM BudgetLineItem bli " +
                    "  LEFT JOIN BudgetLineItemDetail blid ON blid.FK_budgetLineItemId = bli.id " +
                    "  WHERE bli.`year` = :year " +
                    "  AND (bli.FK_createdByUserId = :userId OR bli.FK_checkedByUserId = :userId OR bli.FK_approvedByUserId = :userId) " +
                    "  GROUP BY bli.id " +
                    ") countBudgetLineItems ",
            nativeQuery = true)
    Page<BudgetLineItem> findAllByYear(@Param("year") Integer year,
                                       @Param("userId") Integer userId,
                                       Pageable pageable);

    @Query(value = "SELECT bli.* FROM BudgetLineItem bli " +
            "LEFT JOIN BudgetLineItemDetail blid ON blid.FK_budgetLineItemId = bli.id " +
            "WHERE bli.`year` = :year AND bli.FK_divisionId = :divisionId " +
            "AND (bli.FK_createdByUserId = :userId OR bli.FK_checkedByUserId = :userId OR bli.FK_approvedByUserId = :userId) " +
            "GROUP BY bli.id " +
            "ORDER BY bli.`year` " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "  SELECT bli.* FROM BudgetLineItem bli " +
                    "  LEFT JOIN BudgetLineItemDetail blid ON blid.FK_budgetLineItemId = bli.id " +
                    "  WHERE bli.`year` = :year AND bli.FK_divisionId = :divisionId " +
                    "  AND (bli.FK_createdByUserId = :userId OR bli.FK_checkedByUserId = :userId OR bli.FK_approvedByUserId = :userId) " +
                    "  GROUP BY bli.id " +
                    ") countBudgetLineItems ",
            nativeQuery = true)
    Page<BudgetLineItem> findAllByYearAndDivisionId(@Param("year") Integer year,
                                                    @Param("divisionId") Integer divisionId,
                                                    @Param("userId") Integer userId,
                                                    Pageable pageable);

    @Query(
            value = "SELECT * " +
                    "FROM BudgetLineItem bli " +
                    "WHERE (:selectedYear IS NULL OR bli.`year` = :selectedYear) " +
                    "AND (:selectedDepartmentId IS NULL OR bli.FK_DepartmentId = :selectedDepartmentId) " +
                    "AND (:selectedDivisionId IS NULL OR bli.FK_divisionId = :selectedDivisionId) " +
                    "AND (:selectedStatus IS NULL OR bli.FK_documentStatusId = :selectedStatus) " +
                    "AND ( " +
                    "     bli.FK_createdByUserId = :loggedInUserId " +
                    "  OR bli.FK_checkedByUserId = :loggedInUserId " +
                    "  OR bli.FK_verifiedByUserId = :loggedInUserId " +
                    "  OR bli.FK_approvedByUserId = :loggedInUserId " +
                    ")",
            nativeQuery = true
    )
    List<BudgetLineItem> findAllBySelectedYearAndSelectedDivisionId(
            @Param("selectedYear") Integer selectedYear,
            @Param("selectedDepartmentId") Integer selectedDepartmentId,
            @Param("selectedDivisionId") Integer selectedDivisionId,
            @Param("selectedStatus") Integer selectedStatus,
            @Param("loggedInUserId") Integer loggedInUserId
    );

    @Query(
            value = "SELECT * " +
                    "FROM BudgetLineItem bli " +
                    "WHERE (:selectedYear IS NULL OR bli.`year` = :selectedYear) " +
                    "AND (:selectedDepartmentId IS NULL OR bli.FK_DepartmentId = :selectedDepartmentId) " +
                    "AND (:selectedDivisionId IS NULL OR bli.FK_divisionId = :selectedDivisionId) " +
                    "AND bli.FK_documentStatusId NOT IN (:documentStatusIds) " +
                    "AND ( " +
                    "     bli.FK_createdByUserId = :loggedInUserId " +
                    "  OR bli.FK_checkedByUserId = :loggedInUserId " +
                    "  OR bli.FK_verifiedByUserId = :loggedInUserId " +
                    "  OR bli.FK_approvedByUserId = :loggedInUserId " +
                    ")",
            nativeQuery = true
    )
    List<BudgetLineItem> findAllPendingBySelectedYearAndSelectedDivisionId(
            @Param("selectedYear") Integer selectedYear,
            @Param("selectedDepartmentId") Integer selectedDepartmentId,
            @Param("selectedDivisionId") Integer selectedDivisionId,
            @Param("documentStatusIds") Collection<Integer> documentStatusIds,
            @Param("loggedInUserId") Integer loggedInUserId
    );

    @Query(
            value = "SELECT * " +
                    "FROM BudgetLineItem bli " +
                    "WHERE (:selectedYear IS NULL OR bli.`year` = :selectedYear) " +
                    "AND (:selectedDepartmentId IS NULL OR bli.FK_DepartmentId = :selectedDepartmentId) " +
                    "AND (:selectedDivisionId IS NULL OR bli.FK_divisionId = :selectedDivisionId) " +
                    "AND (:selectedStatus IS NULL OR bli.FK_documentStatusId = :selectedStatus)",
            nativeQuery = true
    )
    List<BudgetLineItem> findAllForFsdManager(
            @Param("selectedYear") Integer selectedYear,
            @Param("selectedDepartmentId") Integer selectedDepartmentId,
            @Param("selectedDivisionId") Integer selectedDivisionId,
            @Param("selectedStatus") Integer selectedStatus
    );

    @Query(
            value = "SELECT * " +
                    "FROM BudgetLineItem bli " +
                    "WHERE (:selectedYear IS NULL OR bli.`year` = :selectedYear) " +
                    "AND (:selectedDepartmentId IS NULL OR bli.FK_DepartmentId = :selectedDepartmentId) " +
                    "AND (:selectedDivisionId IS NULL OR bli.FK_divisionId = :selectedDivisionId) " +
                    "AND bli.FK_documentStatusId NOT IN (:documentStatusIds)",
            nativeQuery = true
    )
    List<BudgetLineItem> findAllPendingForFsdManager(
            @Param("selectedYear") Integer selectedYear,
            @Param("selectedDepartmentId") Integer selectedDepartmentId,
            @Param("selectedDivisionId") Integer selectedDivisionId,
            @Param("documentStatusIds") Collection<Integer> documentStatusIds
    );

    @Query(
            value = "SELECT * " +
                    "FROM BudgetLineItem bli " +
                    "WHERE (:selectedYear IS NULL OR bli.`year` = :selectedYear) " +
                    "AND (:selectedDepartmentId IS NULL OR bli.FK_DepartmentId = :selectedDepartmentId) " +
                    "AND (:selectedDivisionId IS NULL OR bli.FK_divisionId = :selectedDivisionId) " +
                    "AND (:selectedStatus IS NULL OR bli.FK_documentStatusId = :selectedStatus)",
            nativeQuery = true
    )
    List<BudgetLineItem> findAllForGeneralManager(
            @Param("selectedYear") Integer selectedYear,
            @Param("selectedDepartmentId") Integer selectedDepartmentId,
            @Param("selectedDivisionId") Integer selectedDivisionId,
            @Param("selectedStatus") Integer selectedStatus
    );

    List<BudgetLineItem> findAllByYearAndDepartmentIdOrDivisionIdOrCreatedByAccountNoOrderByCreatedAtAsc(Integer year,Integer departmentId,Integer divisionId,Integer createdById);
    List<BudgetLineItem> findAllByYearAndDepartmentIdAndDivisionIdOrderByCreatedAtAsc(Integer year,Integer departmentId,Integer divisionId);

}
