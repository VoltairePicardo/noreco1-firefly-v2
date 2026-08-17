package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.QuotationTerm;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.controller.response.QuotationDto;
import com.noreco1.fireflyv2.controller.response.QuotationListDto;
import com.noreco1.fireflyv2.controller.response.reports.QuotationDetail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface QuotationService extends VoucherService {

    @Transactional(readOnly = true)
    QuotationDto findById(Integer quotationId);

    @Transactional
    List<QuotationListDto> findAll();

    @Transactional(readOnly = true)
    List<QuotationListDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<QuotationListDto> findByDateRangePending(String from, String to);

    List<QuotationDetail> getForQuotationSummary(Integer rivId);

    List<QuotationDetail> getRvDetailByRvId(Integer rivId);

    List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> getRvItemsForQuotation(Integer rivId);

    Map reportMeta();
    List<Map> datasourceAbstractOfQuotation(Integer id);
    List<QuotationTerm> getTerms(Integer id);

    Map getDefaultSignatoryMoreThen100k();

    Map getDefaultSignatoryMoreThen300k();
}
