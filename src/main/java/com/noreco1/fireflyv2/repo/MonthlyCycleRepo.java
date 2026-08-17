package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MonthlyCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonthlyCycleRepo extends JpaRepository<MonthlyCycle, Integer> {
    List<MonthlyCycle> findAllByOrderByYearDescMonthDesc();
    MonthlyCycle findByYearAndMonth(Integer year, Integer month);

    @Query(value = "SELECT DATE_FORMAT(CONCAT(YEAR,'-',month,'-01'), '%M %Y') as yearMonth, month, year FROM MonthlyCycle " +
            "WHERE year BETWEEN YEAR(:startDate) AND YEAR(:endDate) " +
            "AND month BETWEEN MONTH(:startDate) AND MONTH(:endDate) " +
            "AND STATUS = 'OPEN' ", nativeQuery = true)
    public List<Object[]> findOpenByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

}
