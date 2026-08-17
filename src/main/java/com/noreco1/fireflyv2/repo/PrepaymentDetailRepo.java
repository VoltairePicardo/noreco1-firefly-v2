package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PrepaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Personal on 11/9/2015.
 */
public interface PrepaymentDetailRepo extends JpaRepository<PrepaymentDetail, Integer> {
    public List<PrepaymentDetail> findByPrepaymentId(Integer ppId);

    public Long deleteByPrepaymentId(Integer ppId);

    @Query(value = "SELECT " +
            "pp.id, " +
            "ppd.* " +
            "FROM PrepaymentDetail as ppd " +
            "INNER JOIN Prepayment AS pp ON ppd.FK_prepaymentId = pp.id " +
            "WHERE ppd.month = :month AND ppd.year = :year", nativeQuery = true)
    public List<PrepaymentDetail> findBalanceByMonthAndYear(@Param("month") String month, @Param("year") String year);
}
