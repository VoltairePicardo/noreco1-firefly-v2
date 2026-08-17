package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.controller.response.AllocationFactorDto;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;

import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/3/2015.
 */
public interface AllocationFactorDtoer {
    List<AllocationFactorDto> findAll();
    AllocationFactorDto findOne(Integer accountId, Integer effectId);
    List<Map> findLatestOne(Integer accountId);
    List<AllocationFactorDto> findByEffectivityDateId(Integer effDateId);
    List<Map> findAccountAndVoucherDate(Integer accountId, String voucherDate);
}
