package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.model.PettyCashBatch;
import com.noreco1.fireflyv2.model.PettyCashTrans;
import com.noreco1.fireflyv2.model.PettyCashTransBudgetDetail;
import com.noreco1.fireflyv2.model.PettyCashTransDetail;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.PettyCashTransDetailRepo;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PettyCashTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pcv")
public class PettyCashTransController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    PettyCashTransService pettyCashTransService;

    @Autowired
    PettyCashTransDetailRepo pettyCashTransDetailRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @GetMapping(value = "/list")
    
    public List<HashMap> get() {
        return pettyCashTransService.findAll();
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    
    public PostResponse create(@RequestPart(value = "model") @Valid PettyCashTrans pcv, HttpServletRequest request, BindingResult bindingResult) {

        PostResponse response = pettyCashTransService.processCreate(pcv, bindingResult, messageSource, request);

        if (Checker.documentSaved(response)) {
            pettyCashTransService.logNewValue(response.getLogId());
        }

        return response;
    }

    @GetMapping(value = "/batches")
    public List<HashMap> getBatches() {
        return pettyCashTransService.findAllBatches();
    }

    @GetMapping(value = "/check-vouchers")
    public List<HashMap> getCheckVouchers(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to",   required = false) String to,
            @RequestParam(value = "code", required = false) String code) {
        return pettyCashTransService.findCheckVouchers(from, to, code);
    }

    @PostMapping(value = "/batch/create")
    public PostResponse createBatch(@RequestBody PettyCashBatchDto dto, BindingResult bindingResult) {
        return pettyCashTransService.createBatch(dto);
    }

    @PostMapping(value = "/batch/close")
    public PostResponse closeBatch(@RequestBody PettyCashBatchDto dto, BindingResult bindingResult) {
        return pettyCashTransService.processBatch(dto, bindingResult, messageSource);
    }

    @GetMapping(value = "/{id}")

    public HashMap get(@PathVariable Integer id) {
        return pettyCashTransService.findById(id);
    }

    @GetMapping(value = "/other-amounts")
    
    public BigDecimal getOtherAmounts() {
        return pettyCashTransService.findOtherAmounts();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart(value = "model") @Valid PettyCashTrans pettyCashTrans, HttpServletRequest request, BindingResult bindingResult) {
        PostResponse response = pettyCashTransService.processUpdate(pettyCashTrans, bindingResult, messageSource, request, filesToRemove);

        if (Checker.documentSaved(response)) {
            pettyCashTransService.logNewValue(response.getLogId());
        }

        return response;
    }

    @PostMapping(value = "/process")
    
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return pettyCashTransService.process(postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/default-signatories")
    
    public Map defaultSignatories() {
        return pettyCashTransService.defaultSignatories();
    }

    @PostMapping(value = "/replenish")
    
    public PostResponse replenish(@RequestBody ReplenishmentDto replenishment) {
        return pettyCashTransService.replenish(replenishment);
    }

    @RequestMapping(value = "/summary/default-signatories")
    
    public Map summaryDefaultSignatories() {
        return pettyCashTransService.defaultSignatoriesForSummary();
    }

    @GetMapping(value = "/find-all-for-summary-paged")
    public Page<Map> getAllForSummaryPaged(Pageable pageable,
                                          @RequestParam(value = "batch") Integer batch,
                                          @RequestParam(value = "documentStatusId", required = false) Integer documentStatusId,
                                          @RequestParam(value = "officeId", required = false) Integer officeId) {

        return pettyCashTransService.findAllForSummary(batch, documentStatusId, officeId, pageable)
                .map(object -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("date", object[4]);
                    dto.put("code", object[0]);
                    dto.put("payee", object[2]);
                    dto.put("purpose", "");
                    dto.put("amount", object[3]);
                    dto.put("documentStatus", ServiceUtil.getDocumentStatusModel(DocumentStatus.typeFromInt((Integer) object[5])).getStatus());
                    dto.put("pettyCashTransDetails", pettyCashTransDetailRepo.findByPettyCashTransId((Integer) object[7]));
                    return dto;
                });
    }

    @GetMapping(value = "/{documentStatusId}/paged")
    public Page<PettyCashTrans> byStatusPaged(Pageable pageable,
                                              @PathVariable Integer documentStatusId,
                                              @RequestParam(value = "q", required = false) String filter) {
        return pettyCashTransService.findByStatusAndFilter(documentStatusId, filter, pageable);
    }

    @GetMapping(value = "/is-document-for-cash-flow-item-assignment/{transactionId}")
    
    public boolean isDocumentForCashFlowItemAssignment(@PathVariable Integer transactionId) {
        return pettyCashTransService.isDocumentForCashFlowItemAssignment(transactionId);
    }

    @PostMapping(value = "/saveCashFlowItem")
    
    public PostResponse saveCashFlowItem(@Valid @RequestBody CashFlowItemDto dto, BindingResult bindingResult) {
        return pettyCashTransService.saveCashFlowItem(dto, bindingResult, messageSource);
    }

    @GetMapping(value = "/cash-flow/{pcvId}")
    
    public List<PettyCashTransBudgetDetail> getPettyCashTransBudgetDetails(@PathVariable Integer pcvId, HttpServletRequest request) {
        return pettyCashTransService.getPettyCashTransBudgetDetail(pcvId);
    }

}
