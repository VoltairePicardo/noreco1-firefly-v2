package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.RvDetailDto;
import com.noreco1.fireflyv2.service.PurchaseRequestDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by Personal on 4/29/2015.
 */
@RestController
@RequestMapping("/api/rv-detail")
public class PurchaseRequestDetailController {

    @Autowired
    PurchaseRequestDetailService purchaseRequestDetailService;

    @GetMapping(value = "/rvd/{rvId}")
    
    public List<RvDetailDto> getRvDetails(@PathVariable Integer rvId, HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetails(rvId);
    }

    @GetMapping(value = "/rvd/status/{statusId}")
    
    public List<RvDetailDto> getRvDetailsByStatus(@PathVariable Integer statusId, HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsByStatus(statusId);
    }

    @GetMapping(value = "/rvd/type/po")
    
    public List<RvDetailDto> getRvDetailsForPo(HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsForPo();
    }

    // to be used for creating new PO out of items of a cancelled PO
    @GetMapping(value = "/rvd/type/po/{cancelledPoId}/cancelled")
    
    public List<RvDetailDto> getRvDetailsForPoCancelled(@PathVariable Integer cancelledPoId) {
        return purchaseRequestDetailService.getRvDetailsForPo(cancelledPoId);
    }

    @GetMapping(value = "/rvd/type/po/ro/{supplierAcctNo}")
    
    public List<RvDetailDto> getRvDetailsForPoRo(@PathVariable Integer supplierAcctNo, HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsForPoRo(supplierAcctNo);
    }

    @GetMapping(value = "/rvd/type/jo")
    
    public List<RvDetailDto> getRvDetailsForJo(HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsForJo();
    }

    @GetMapping(value = "/rvd/type/canvass")
    public List<RvDetailDto> getRvDetailsForCanvass(HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsForCanvass();
    }

    @GetMapping(value = "/rvd/type/quotation")

    public List<RvDetailDto> getRvDetailsForQuotation(HttpServletRequest request) {
        return purchaseRequestDetailService.getRvDetailsForQuotation();
    }

    @GetMapping(value = "/rvd/type/withdrawal/{rvId}/{invLocId}/{invCatId}")
    
    public List<Map> getRvDetailsForWithdrawal(@PathVariable Integer rvId,
                                               @PathVariable Integer invLocId,
                                               @PathVariable Integer invCatId) {
        return purchaseRequestDetailService.getRvDetailsForWithdrawal(rvId, invLocId, invCatId);
    }

    @GetMapping(value = "/rvd/type/rr/{rvId}")
    
    public List<Map> getRvDetailsForRR(@PathVariable Integer rvId) {
        return purchaseRequestDetailService.getRvDetailsForRR(rvId);
    }

    @GetMapping(value = "/prd/{prId}/{supplierAccountNumber}")
    
    public List<RvDetailDto> getPrDetails(@PathVariable Integer prId, @PathVariable Integer supplierAccountNumber) {
        return purchaseRequestDetailService.getPrDetailsForPo(prId, supplierAccountNumber);
    }

    @GetMapping(value = "/prd-for-canvass/{prId}")

    public List<RvDetailDto> getPrDetailsForCanvass(@PathVariable Integer prId) {
        return purchaseRequestDetailService.getPrDetailsForCanvass(prId);
    }

    @GetMapping(value = "/purchase-request/for-jo")
    public List<Map<String, Object>> getPurchaseRequestsForJo() {
        return purchaseRequestDetailService.getPurchaseRequestsForJo();
    }

    @GetMapping(value = "/purchase-request/for-po")
    public List<Map<String, Object>> getPurchaseRequestsForPo() {
        return purchaseRequestDetailService.getPurchaseRequestsForPo();
    }
}