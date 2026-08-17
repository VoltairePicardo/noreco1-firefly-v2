package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.QuotationDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI on 7/1/2019.
 */
public interface QuotationDetailService {
    List<QuotationItemDto> getQuotationDetails(Integer quotationId);
    BigDecimal getItemQuotationPrice(Integer supplierAccountNo, Integer rvDetailId);
    Map itemDetailForPO(Integer rvDetailId);
}
