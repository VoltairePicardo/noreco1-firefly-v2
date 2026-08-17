package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AdjustmentJournal;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto2;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by User on 11/15/2016.
 */
public interface AjService extends VoucherService {

    public List<Map> findAll();
    public Map findById(Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRange(String from, String to);

    @Transactional(readOnly = true)
    List<GeneralLedgerLineDto2> closingDefaultEntries(String asOfDate);

    @Transactional(readOnly = true)
    List<GeneralLedgerLineDto2> reopeningDefaultEntries(String year);

    @Transactional
    PostResponse updateEntries(AdjustmentJournal aj, BindingResult bindingResult, MessageSource messageSource);
}
