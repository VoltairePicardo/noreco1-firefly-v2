package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DateRange;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface DateRangeRepo extends JpaRepository<DateRange, Integer> {
    List<DateRange> findByOrderByEndDesc(Pageable pageable);
    List<DateRange> findByOrderByEndDesc();
    List<DateRange> findByStartBetween(Date from1, Date to1);
    List<DateRange> findByEndBetween(Date from1, Date to1);
    List<DateRange> findAllByOrderByEndDescStartDesc();
    List<DateRange> findByEndGreaterThanEqualAndStartLessThanEqual(Date date1, Date date2);
}
