package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.MonthlyCycle;
import com.noreco1.fireflyv2.model.MonthlyCycleLog;

import java.util.List;
import java.util.Map;

public interface MonthlyCycleClosingService extends DataManagementService {
    List<MonthlyCycle> findAll();
    MonthlyCycle findById(Integer id);
    List<Map> findLogs(Integer id);
    Map findByYearAndMonth(Integer year, Integer month);
}
