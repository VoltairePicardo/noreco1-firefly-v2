package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.SalesVoucher;
import com.noreco1.fireflyv2.controller.response.*;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface SalesVoucherService extends VoucherService {

    @Transactional
    public List<SalesVoucherListDto> findAll();

    @Transactional(readOnly = true)
    public SalesVoucherDto findById(Integer id);

    @Transactional(readOnly = true)
    public  List<SalesVoucherListDto> findByStatusId(Integer id);

    @Transactional(readOnly = true)
    List<SalesVoucherListDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<SalesVoucherListDto> findByDateRange(String from, String to);

    @Transactional
    PostResponse updateEntries(SalesVoucher salesVoucher, BindingResult bindingResult, MessageSource messageSource);
}
