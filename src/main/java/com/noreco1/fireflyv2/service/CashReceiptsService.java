package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CashReceipts;
import com.noreco1.fireflyv2.controller.response.CashReceiptsDto;
import com.noreco1.fireflyv2.controller.response.CashReceiptsListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface CashReceiptsService extends VoucherService {

    @Transactional
    public List<CashReceiptsListDto> findAll();

    @Transactional(readOnly = true)
    public CashReceiptsDto findById(Integer id);

    @Transactional(readOnly = true)
    public  List<CashReceiptsListDto> findByStatusId(Integer id);

    @Transactional(readOnly = true)
    List<CashReceiptsListDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<CashReceiptsListDto> findByDateRange(String from, String to);

    @Transactional
    PostResponse updateEntries(CashReceipts cashReceipts, BindingResult bindingResult, MessageSource messageSource);
}
