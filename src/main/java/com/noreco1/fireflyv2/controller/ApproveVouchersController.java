package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.ApproveDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.VoucherInstantApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approve-vouchers")
public class ApproveVouchersController {

    @Autowired
    VoucherInstantApprovalService approvalService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/vouchers")
    public List<Map> vouchers() {
        return approvalService.pendingVouchers();
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ApproveDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "approveDocumentDto");
        return approvalService.process(postData, bindingResult, messageSource);
    }

    @PostMapping("/process-all")
    public PostResponse processAll(@RequestBody List<ApproveDocumentDto> postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "approveDocumentDtos");
        return approvalService.approveAll(postData, bindingResult, messageSource);
    }

    @PostMapping("/process-all-budget")
    public PostResponse processAllBudget(@RequestBody List<ApproveDocumentDto> postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "approveDocumentDtos");
        return approvalService.approveAllBudget(postData, bindingResult, messageSource);
    }
}
