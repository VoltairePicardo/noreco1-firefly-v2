package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.controller.response.VoucherCashflowItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.VoucherCashflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/voucher-cash-flow")
public class CashflowVoucherSetupController {

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    VoucherCashflowService voucherCashflowService;

    @Autowired
    MessageSource messageSource;

    @PostMapping(value = "/set/{voucherCodePrefix}")
    
    public PostResponse setCashflow(@PathVariable String voucherCodePrefix, @RequestBody VoucherCashflowItemDto postData, BindingResult bindingResult) {
        return voucherCashflowService.setVoucherCashflow(voucherCodePrefix, postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/details/{transId}")
    
    public List<Map> getVoucherCashflowDetails(@PathVariable Integer transId) {
        return documentDtoer.getVoucherCashflowDetail(transId);
    }

    @GetMapping(value = "/approved-vouchers/{option}/{from}/{to}")
    
    public List<Map> getApprovedVouchersWithOption(@PathVariable(value = "option") String option, @PathVariable String from, @PathVariable String to) {
        Integer o = null;
        try {
            o = Integer.parseInt(option);
        } finally {}
        return documentDtoer.getApprovedVouchersForCashflow(o, from, to);
    }

    @GetMapping(value = "/all-vouchers/{from}/{to}")
    
    public List<Map> getAllVouchersWithOption(@PathVariable String from, @PathVariable String to) {
        return documentDtoer.getAllVouchersForCashflow(from, to);
    }

    @GetMapping(value = "/{documentTypeCode}/{voucherId}")
    
    public Map getVoucher(@PathVariable String documentTypeCode, @PathVariable Integer voucherId) {
        return documentDtoer.getVoucherForCashflowSetup(voucherId, documentTypeCode);
    }

    @RequestMapping(value = "/default-signatories")
    
    public Map defaultSignatories() {
        return voucherCashflowService.defaultSignatories();
    }
}
