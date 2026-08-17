package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.VoucherCashflowItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Map;

public interface VoucherCashflowService extends DataManagementService {

    @Transactional
    public PostResponse setVoucherCashflow(String voucherCodePrefix, VoucherCashflowItemDto postData, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    public Map defaultSignatories();
}
