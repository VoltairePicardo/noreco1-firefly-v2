package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.ApproveDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface VoucherInstantApprovalService {

    List<Map> pendingVouchers();

    @Transactional
    PostResponse process(ApproveDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse approveAll(List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse approveAllBudget(List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult, MessageSource messageSource);
}
