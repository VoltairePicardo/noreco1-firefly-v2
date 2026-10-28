package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetReportRepo extends JpaRepository<BudgetDetail, Integer> {

    /*@Query(value = "select " +
            "bd.id as id, " +
            "b.id as FK_budgetId, " +
            "bd.FK_cashflowItemId, " +
            "bd.amount, " +
            "sum(if (b.FK_departmentId = 1, bd.amount, 0)) as amtOgm, " +
            "sum(if (b.FK_departmentId = 2, bd.amount, 0)) as amtAod, " +
            "sum(if (b.FK_departmentId = 3, bd.amount, 0)) as amtIsd, " +
            "sum(if (b.FK_departmentId = 4, bd.amount, 0)) as amtFsd, " +
            "sum(if (b.FK_departmentId = 5, bd.amount, 0)) as amtTsd, " +
            "sum(if (b.FK_departmentId = 6, bd.amount, 0)) as amtBod, " +
            "(select " +
            "sum(bd.amount) as totalBudgetPerDept " +
            "from BudgetDetail bd " +
            "inner join Budget b on bd.FK_budgetId = b.id " +
            "inner join CashflowItem cic on bd.FK_cashflowItemId = cic.id " +
            "inner join CashflowItem cip on cic.FK_parentId = cip.id " +
            "where b.year = :budgetYear " +
            "group by cic.id, cip.id " +
            "order by cip.name, cic.name, b.id, bd.id, b.voucherDate",
            nativeQuery = true)
    List<BudgetDetail> findByBudgetYear(@Param("budgetYear") Integer budgetYear); */// original query

    @Query(value = "select " +
                    "bd.id as id, " +
                    "b.id as FK_budgetId, " +
                    "bd.FK_cashflowItemId, " +
                    "bd.amount, " +
                    "0 as amtOgm, " +
                    "0 as amtAod, " +
                    "0 as amtIsd, " +
                    "0 as amtFsd, " +
                    "0 as amtTsd, " +
                    "0 as amtBod, " +
                    "(select sum(bd2.amount) " +
                    "from BudgetDetail bd2 " +
                    "inner join Budget b2 on bd2.FK_budgetId = b2.id " +
                    "inner join CashflowItem cic2 on bd2.FK_cashflowItemId = cic2.id " +
                    "inner join CashflowItem cip2 on cic2.FK_parentId = cip2.id " +
                    "where b2.year = :budgetYear) as totalYearBudget " +
                    "from BudgetDetail bd " +
                    "inner join Budget b on bd.FK_budgetId = b.id " +
                    "inner join CashflowItem cic on bd.FK_cashflowItemId = cic.id " +
                    "inner join CashflowItem cip on cic.FK_parentId = cip.id " +
                    "where b.year = :budgetYear " +
                    "group by cic.id, cip.id " +
                    "order by cip.name, cic.name, b.id, bd.id",
            nativeQuery = true)
    List<BudgetDetail> findByBudgetYear(@Param("budgetYear") Integer budgetYear);

}
