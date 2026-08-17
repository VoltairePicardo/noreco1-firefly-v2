package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.ApproveDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.VoucherInstantApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/instant-approve")
public class VoucherInstantApprovalController {

    @Autowired
    VoucherInstantApprovalService approvalService;

    @Autowired
    MessageSource messageSource;

    // actions
    @PostMapping(value = "/process")
    
    public PostResponse createAccount(@RequestBody ApproveDocumentDto postData, BindingResult bindingResult) {
        return approvalService.process(postData, bindingResult, messageSource);
    }

    @PostMapping(value = "/process-all")
    
    public PostResponse approveAllSelected(@RequestBody List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult) {
        return approvalService.approveAll(approveDocumentDtos, bindingResult, messageSource);
    }

    @PostMapping(value = "/process-all-documents")
    
    public PostResponse approveAllSelectedDocuments(@RequestBody List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult) {
        return approvalService.approveAllBudget(approveDocumentDtos, bindingResult, messageSource);
    }

    // data providers
    @GetMapping(value = "/vouchers")
    
    public List<Map> vouchers() {
        return approvalService.pendingVouchers();
    }
}
