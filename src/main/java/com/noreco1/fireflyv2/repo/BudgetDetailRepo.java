package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetDetailRepo extends JpaRepository<BudgetDetail, Integer> {
    @Query(value = "select " +
            "bd.id, " +
            "bd.FK_budgetId, " +
            "bd.FK_cashflowItemId, " +
            "bd.amount," +
            "0 as amtOgm, " +
            "0 as amtAod, " +
            "0 as amtIsd, " +
            "0 as amtFsd, " +
            "0 as amtTsd, " +
            "0 as amtBod, " +
            "0 as totalYearBudget " +
            "from BudgetDetail bd " +
            "where bd.FK_budgetId = :budgetId",
            nativeQuery = true)
    List<BudgetDetail> findByBudgetId(@Param("budgetId") Integer budgetId);

    @Query(value = "select " +
            "bd.id as id, " +
            "b.id as FK_budgetId, " +
            "bd.FK_cashflowItemId, " +
            "bd.amount, " +
            "sum(if (b.FK_departmentId = 1, bd.amount, 0)) as amtOgm, " +
            "sum(if (b.FK_departmentId = 2, bd.amount, 0)) as amtAod, " +
            "sum(if (b.FK_departmentId = 3, bd.amount, 0)) as amtIsd, " +
            "sum(if (b.FK_departmentId = 4, bd.amount, 0)) as amtFsd, " +
            "sum(if (b.FK_departmentId = 5, bd.amount, 0)) as amtTsd, " +
            "sum(if (b.FK_departmentId = 6, bd.amount, 0)) as amtBod " +
            "from BudgetDetail bd " +
            "inner join Budget b on bd.FK_budgetId = b.id " +
            "inner join Department d on b.FK_departmentId = d.id " +
            "inner join CashflowItem cic on bd.FK_cashflowItemId = cic.id " +
            "inner join CashflowItem cip on cic.FK_parentId = cip.id " +
            "where b.year = :budgetYear " +
            "group by bd.id, b.id, cic.id, cip.id " +
            "order by cip.name, cic.name, b.id, bd.id, b.voucherDate",
            nativeQuery = true)
    List<BudgetDetail> findByBudgetYear(@Param("budgetYear") Integer budgetYear);

    Long deleteByBudgetId(Integer id);

    @Query(value = "SELECT COALESCE(SUM(bd.amount), 0) FROM BudgetDetail bd WHERE bd.FK_budgetId = :budgetId", nativeQuery = true)
    BigDecimal sumAmountByBudgetId(@Param("budgetId") Integer budgetId);

    @Query(value = "SELECT " +
            "    bd.* " +
            "FROM BudgetDetail bd " +
            "INNER JOIN Budget b  " +
            "    ON b.id = bd.FK_budgetId " +
            "INNER JOIN cashflowitem c  " +
            "    ON c.id = bd.FK_cashflowItemId " +
            "INNER JOIN cashflowitemtype t  " +
            "    ON t.id = c.FK_cashFlowItemTypeId " +
            "LEFT JOIN cashflowitem p  " +
            "    ON c.FK_parentId = p.id " +
            "WHERE b.`year` = :budgetYear " +
            "ORDER BY c.ordinalNumber",
            nativeQuery = true)
    List<BudgetDetail> findAllByBudgetYear(@Param("budgetYear") Integer budgetYear);

    @Query(value = "SELECT " +
            "    bd.amount - ( " +
            "        COALESCE((SELECT SUM(cv.amount) " +
            "                  FROM CheckVoucher cv " +
            "                  WHERE cv.FK_budgetDetailId = bd.id), 0) + " +
            "        COALESCE((SELECT SUM(jvcfbd.totalAmount) " +
            "                  FROM JournalVoucherCashFlowBudgetDetail jvcfbd " +
            "                  INNER JOIN JournalVoucher jv ON jv.id = jvcfbd.FK_journalVoucherId " +
            "                  WHERE jvcfbd.FK_budgetDetailId = bd.id), 0) " +
            "    ) AS balance " +
            "FROM BudgetDetail bd " +
            "WHERE bd.id = :budgetDetailId", nativeQuery = true)
    BigDecimal getBudgetDetailBalance(@Param("budgetDetailId") Integer budgetDetailId);

}
