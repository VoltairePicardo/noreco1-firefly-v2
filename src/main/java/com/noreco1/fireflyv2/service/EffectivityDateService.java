package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by yer on 8/8/2016.
 */
public interface EffectivityDateService extends DataManagementService {
    List<DateRange> findByStartRange(Date start, Date end);
    List<DateRange> findByEndRange(Date start, Date end);
    List<DateRange> findOverlapping(Date start, Date end);
    DateRange findById(Integer id);
    List<DateRange> findAll();

    @Transactional
    PostResponse remove(Integer id);
}
