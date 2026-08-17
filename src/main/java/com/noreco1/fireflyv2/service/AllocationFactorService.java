package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.AllocationFactorDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AllocationFactorService extends DataManagementService {
    List<AllocationFactorDto> findAll();
    AllocationFactorDto findByAccountAndEffectivityId(Integer accountId, Integer effectId);
    List<Map> findLatestByAccount(Integer accountId);
    List<Map> findAccountAndEffectDate(Integer accountId, String voucherDate);
    List<Map> findDateRanges();
    Map testAutoAllocation(Map postData);
    List<AllocationFactorDto> findByEffectivityDateId(Integer effDateId);
    boolean isAccountFactorManual(int accountId);
}
