package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AccountsPayableVoucher;
import com.noreco1.fireflyv2.model.AccountsPayableVoucherLink;
import com.noreco1.fireflyv2.controller.response.ApvDto;
import com.noreco1.fireflyv2.controller.response.ApvListDto;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface ApvService extends VoucherService {

    @Transactional(readOnly = true)
    List<ApvListDto> findAll();

    @Transactional(readOnly = true)
    ApvDto findById(Integer id);

    @Transactional(readOnly = true)
    List<ApvListDto> findByStatusId(Integer id);

    @Transactional(readOnly = true)
    List<ApvListDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<ApvListDto> findByDateRange(String from, String to);

    Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable);

    PostResponse updateUnpaidRemarks(AccountsPayableVoucher apv);

    @Transactional
    PostResponse updateEntries(AccountsPayableVoucher apv, BindingResult bindingResult, MessageSource messageSource);

}
