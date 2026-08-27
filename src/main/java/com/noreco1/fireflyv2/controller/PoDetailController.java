package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import com.noreco1.fireflyv2.controller.response.PoDetailDto;
import com.noreco1.fireflyv2.service.PoDetailService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Personal on 5/15/2015.
 */
@RestController
@RequestMapping("/api/po-detail")
public class PoDetailController {

    private final PoDetailService poDetailService;

    public PoDetailController(PoDetailService poDetailService) {
        this.poDetailService = poDetailService;
    }

    @GetMapping(value = "/pod/{poId}")
    public List<PoDetailDto> getPoDetails(@PathVariable Integer poId, HttpServletRequest request) {
        return poDetailService.getPoDetails(poId);
    }

    @GetMapping(value = "/canvass-price/{supplierAccountNo}/{rvDetailId}")
    public BigDecimal getPoDetails(@PathVariable Integer supplierAccountNo, @PathVariable Integer rvDetailId, HttpServletRequest request) {
        return poDetailService.getItemCanvassPrice(supplierAccountNo, rvDetailId);
    }

    @GetMapping(value = "/pod-for-item-testing/{poId}")
    public List<PoDetailDto> getPoDetailsForItemTesting(@PathVariable Integer poId, HttpServletRequest request) {
        return poDetailService.getPoDetailsForItemTesting(poId);
    }

    @GetMapping(value = "/pod-with-item-testing/{poId}")
    public List<PoDetailDto> getPoDetailsWithItemTesting(@PathVariable Integer poId, HttpServletRequest request) {
        return poDetailService.getPoDetailsWithItemTesting(poId);
    }

    @GetMapping(value = "/cash-flow/{poId}")
    public List<PurchaseOrderBudgetDetail> getPurchaseOrderBudgetDetails(@PathVariable Integer poId, HttpServletRequest request) {
        return poDetailService.getPurchaseOrderBudgetDetail(poId);
    }

    @GetMapping(value = "/cash-flow/amount-balance/{id}/{type}")
    public BigDecimal getCashFlowItemAmountBalanceByType(@PathVariable Integer id,@PathVariable String type) {
        return this.poDetailService.getCashFlowItemAmountBalanceByType(id, type);
    }

    @GetMapping(value = "/get-default-estimated-amount")
    public BigDecimal getDefaultEstimatedAmount(@RequestParam List<Integer> itemIds) {
        return poDetailService.getDefaultEstimatedAmount(itemIds);
    }

}
