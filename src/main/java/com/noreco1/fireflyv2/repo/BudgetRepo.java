package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface BudgetRepo extends JpaRepository<Budget, Integer> {
    @Query(value = "select " +
            "* " +
            "from Budget b " +
            "group by b.year " +
            "order by b.year asc",
            nativeQuery = true)
    List<Budget> getBudgetYears();

    @Query(value = "SELECT " +
            "SUM(COALESCE(BudgetDetail.amount,0)) as budget, " +
            "FK_cashflowItemId as cashflowItemId " +
            "FROM Budget " +
            "JOIN BudgetDetail ON Budget.id = BudgetDetail.FK_budgetId " +
            "WHERE BudgetDetail.FK_cashflowItemId = :cashflowItemId " +
            "AND `year` = :year " +
            "GROUP BY BudgetDetail.FK_cashflowItemId LIMIT 1", nativeQuery = true)
    public List<Object[]> findByCashflowItemIdAndDateRange(@Param("cashflowItemId") Integer cashflowItemId,
                                                           @Param("year") Integer year);

//    List<Budget> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

//    Budget findOneByTransactionId(Integer transId);

    @Query(value = "SELECT  " +
            "COALESCE(SUM(BudgetDetail.amount), 0) " +
            "FROM Budget  " +
            "JOIN BudgetDetail ON Budget.id = BudgetDetail.FK_budgetId " +
            "WHERE Budget.`year` = :year " +
            "AND BudgetDetail.FK_cashflowItemId = :cashflowItemId", nativeQuery = true)
    BigDecimal sumByYearAndCfItemId(@Param("year") Integer year,
                                    @Param("cashflowItemId") Integer cashflowItemId);
}
