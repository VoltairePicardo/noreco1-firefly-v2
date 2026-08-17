package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.TurnOnOrderWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnOnOrderWithdrawalRepo extends JpaRepository<TurnOnOrderWithdrawal, Integer> {

    @Query(value = "SELECT tow.id, tow.date, COUNT(tow.id) as totalTurnOnOrders, u.fullName FROM TurnOnOrderWithdrawal tow " +
            "LEFT JOIN TurnOnOrderWithdrawalDetail towd ON towd.FK_turnOnOrderWithdrawalId = tow.id " +
            "LEFT JOIN Users u ON tow.FK_createdByUserId = u.id " +
            "WHERE tow.date BETWEEN :startDate AND :endDate " +
            "GROUP BY tow.id, tow.date, u.fullName " +
            "ORDER BY tow.date  \n-- #pageable\n",
            countQuery = "select count(*) from TurnOnOrderWithdrawal tow " +
                    "LEFT JOIN TurnOnOrderWithdrawalDetail towd ON towd.FK_turnOnOrderWithdrawalId = tow.id " +
                    "LEFT JOIN Users u ON tow.FK_createdByUserId = u.id " +
                    "WHERE tow.date BETWEEN :startDate AND :endDate " +
                    "GROUP BY tow.id, tow.date, u.fullName ", nativeQuery = true)
    Page<Object[]> findAllForWithdrawal(@Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     Pageable pageable);

    @Query(value = "SELECT COUNT(*) as totalTurnOnOrders FROM TurnOnOrderWithdrawalDetail " +
            "WHERE FK_turnOnOrderWithdrawalId = ?1 ", nativeQuery = true)
    Integer getTotalTurnOnOrderByTurnOnOrderWithdrawalId(Integer id);

}
