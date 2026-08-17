package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.QuotationDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDto;
import com.noreco1.fireflyv2.service.QuotationDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI on 7/1/2019.
 */
@RestController
@RequestMapping("/quotation-detail")
public class QuotationDetailController {

    @Autowired
    QuotationDetailService quotationDetailService;

    @GetMapping(value = "/{quotationId}")
    
    public List<QuotationItemDto> getQuotationDetails(@PathVariable Integer quotationId, HttpServletRequest request) {
        return quotationDetailService.getQuotationDetails(quotationId);
    }

    @GetMapping(value = "/quotation-price/{supplierAccountNo}/{rvDetailId}")
    
    public BigDecimal getQuotationPrice(@PathVariable Integer supplierAccountNo, @PathVariable Integer rvDetailId, HttpServletRequest request) {
        return quotationDetailService.getItemQuotationPrice(supplierAccountNo, rvDetailId);
    }

    @GetMapping(value = "/po/{rvDetailId}")
    
    public Map getQuotationDetails(@PathVariable Integer rvDetailId) {
        return quotationDetailService.itemDetailForPO(rvDetailId);
    }

}
