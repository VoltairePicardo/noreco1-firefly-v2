package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.JournalVoucher;
import com.noreco1.fireflyv2.model.JournalVoucherBudgetLineItemDetail;
import com.noreco1.fireflyv2.model.JournalVoucherBudgetSubItemDetail;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.JournalVoucherAdditionalBudgetLineItemDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface JvService extends VoucherService {

    public List<Map> findAll();
    public Map findById(Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRange(String from, String to);

    @Transactional
    PostResponse updateEntries(JournalVoucher journalVoucher, BindingResult bindingResult, MessageSource messageSource);

    Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable);

    List<JournalVoucherBudgetLineItemDetail> getJournalVoucherBudgetLineItemDetails(Integer jvId);

    List<JournalVoucherBudgetSubItemDetail> getJournalVoucherBudgetSubItemDetails(Integer jvId);

    @Transactional
    PostResponse additionalBudgetLineItemDetail(JournalVoucherAdditionalBudgetLineItemDetailDto detailDto, BindingResult bindingResult, MessageSource messageSource);
}
