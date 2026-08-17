package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Prepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 6/3/2015.
 */
public interface PrepaymentRepo extends JpaRepository<Prepayment, Integer> {
    public List<Prepayment> findByDescription(String description);
    Prepayment findByAccountNo(Integer accountNo);

    @Query(value = "SELECT " +
            "pp.*, " +
            "pp.FK_prepaymentAccountId, " +
            "pp.FK_expenseAccountId, " +
            "(SELECT " +
            "title " +
            "FROM Account a " +
            "WHERE a.id = pp.FK_prepaymentAccountId LIMIT 1) AS prepayment_acct, " +
            "(SELECT " +
            "title " +
            "FROM Account a " +
            "WHERE a.id = pp.FK_expenseAccountId) AS expense_acct " +
            "FROM Prepayment AS pp " +
            "WHERE (DATE(:startDateCreated) BETWEEN DATE_ADD(DATE_FORMAT(pp.datePaid, '%Y-%m-01'), INTERVAL 1 MONTH) " +
            "AND ADDDATE(DATE_FORMAT(pp.datePaid, '%Y-%m-01'), INTERVAL pp.noOfMonths MONTH)) " +
            "AND pp.id NOT IN " +
            "(SELECT ppp.FK_prepaymentId " +
            "FROM PrepaymentDetail AS ppp " +
            "WHERE CONCAT(ppp.year, ppp.month) = :concatYearMonth) " +
            "AND pp.balance > 0 " +
            "AND pp.FK_accountNo IN " +
            "(SELECT sl.FK_accountNo " +
            "FROM SubLedger sl " +
            "WHERE sl.FK_accountNo = pp.FK_accountNo);", nativeQuery = true)
    public List<Prepayment> findByStartDateCreatedAndMonthYear(@Param("startDateCreated") String startDateCreated, @Param("concatYearMonth") String concatYearMonth);

    @Query(value = "SELECT e.code FROM Prepayment e WHERE year = :year  AND code LIKE '%PP%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestPpCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    @Query(value = "SELECT " +
            "pp.*, " +
            "(SELECT " +
            "title " +
            "FROM Account a " +
            "WHERE a.id = pp.FK_prepaymentAccountId LIMIT 1) AS prepayment_acct, " +
            "(SELECT " +
            "title " +
            "FROM Account a " +
            "WHERE a.id = pp.FK_expenseAccountId) AS expense_acct " +
            "FROM Prepayment AS pp " +
            "INNER JOIN PrepaymentDetail as ppd ON ppd.FK_prepaymentId = pp.id " +
            "WHERE ppd.month = :month AND ppd.year = :year", nativeQuery = true)
    public List<Prepayment> findByMonthAndYear(@Param("month") String month, @Param("year") String year);

    @Query(value = "SELECT " +
            "pp.* " +
            "FROM Prepayment AS pp " +
            "WHERE pp.createdAt BETWEEN :startDate AND :endDate", nativeQuery = true)
    public List<Prepayment> findByStartAndEndDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT " +
            "pp.code, " +
            "pp.description, " +
            "pp.noOfMonths, " +
            "ppd.amount, " +
            "ppd.balance " +
            "FROM Prepayment AS pp " +
            "INNER JOIN PrepaymentDetail as ppd ON ppd.FK_prepaymentId = pp.id " +
            "WHERE ppd.month = :month AND ppd.year = :year", nativeQuery = true)
    public List<Object[]> findByMonthAndYearObject(@Param("month")String month, @Param("year")String year);

    List<Prepayment> findByCreatedAtBetweenAndBalanceGreaterThan(@Param("startDate") Date startDate,
                                                                 @Param("endDate") Date endDate,
                                                                 @Param("balance") BigDecimal balance);

    @Query(value = "SELECT  pp.* FROM Prepayment AS pp " +
            "WHERE pp.createdAt BETWEEN :startDate AND :endDate " +
            "AND (pp.balance > 0 " +
            "OR pp.id NOT IN (SELECT ppd.FK_prepaymentId FROM PrepaymentDetail ppd WHERE ppd.FK_prepaymentId = pp.id))", nativeQuery = true)
    List<Prepayment> findAllOpenByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT  pp.* FROM Prepayment AS pp " +
            "WHERE pp.balance > 0 " +
            "OR pp.id NOT IN (SELECT ppd.FK_prepaymentId FROM PrepaymentDetail ppd WHERE ppd.FK_prepaymentId = pp.id)", nativeQuery = true)
    List<Prepayment> findAllOpen();

    @Query(value = "SELECT  pp.* FROM Prepayment AS pp " +
            "WHERE pp.balance = 0 " +
            "AND pp.createdAt BETWEEN :startDate AND :endDate " +
            "AND pp.id IN (SELECT ppd.FK_prepaymentId FROM PrepaymentDetail ppd WHERE ppd.FK_prepaymentId = pp.id)", nativeQuery = true)
    List<Prepayment> findAllClosedByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT  pp.* FROM Prepayment AS pp " +
            "WHERE pp.balance = 0 " +
            "AND pp.id IN (SELECT ppd.FK_prepaymentId FROM PrepaymentDetail ppd WHERE ppd.FK_prepaymentId = pp.id)", nativeQuery = true)
    List<Prepayment> findAllClosed();

    List<Prepayment> findByCreatedAtBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
