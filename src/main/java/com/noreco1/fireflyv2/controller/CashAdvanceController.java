package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.CashFlowItemDto;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.model.CashAdvance;
import com.noreco1.fireflyv2.model.CashAdvanceBudgetDetail;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.repo.BudgetLineItemDetailRepo;
import com.noreco1.fireflyv2.repo.PurchaseOrderBudgetDetailRepo;
import com.noreco1.fireflyv2.service.CashAdvanceService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/cash-advance")
public class CashAdvanceController {

    @Autowired
    @Qualifier("cashAdvanceServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("cashAdvanceServiceImpl")
    CashAdvanceService cashAdvanceService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    private PurchaseOrderBudgetDetailRepo purchaseOrderBudgetDetailRepo;

    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "/export/{id}")
    public void exportToPdf(
            @PathVariable Integer id,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "token") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CashAdvance.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @GetMapping("/list")
    public List<HashMap> list() {
        return cashAdvanceService.findAll();
    }

    @GetMapping("/list/by-status/{statusId}")
    public List<HashMap> listByStatus(@PathVariable Integer statusId) {
        return cashAdvanceService.findByStatusId(statusId);
    }

    @GetMapping("/list/date-range")
    public List<HashMap> listByDateRange(@RequestParam String from,
                                         @RequestParam String to,
                                         @RequestParam(required = false) Integer statusId,
                                         @RequestParam(required = false) Integer officeId) {
        if (statusId != null) {
            return cashAdvanceService.findByDateRangeAndStatusId(from, to, statusId, officeId);
        }
        return cashAdvanceService.findByDateRange(from, to, officeId);
    }

    @GetMapping("/approved-for-cv-paged")
    public Page<CvVoucherDto> approvedForCvPaged(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return cashAdvanceService.findAllApprovedForCvPaged(q.isEmpty() ? null : q, PageRequest.of(page, size));
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return cashAdvanceService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public HashMap getData(@PathVariable Integer id) {
        return cashAdvanceService.findById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart("model") @Valid CashAdvance cashAdvance,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = cashAdvanceService.processCreate(cashAdvance, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            cashAdvanceService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart("model") @Valid CashAdvance cashAdvance,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = cashAdvanceService.processUpdate(cashAdvance, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            cashAdvanceService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return cashAdvanceService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return cashAdvanceService.defaultSignatories();
    }

    @GetMapping("/unliquidated/list")
    public List<CashAdvance> unliquidatedList() {
        return cashAdvanceService.unliquidatedList();
    }

    @PostMapping("/liquidated/{id}")
    public PostResponse setAsLiquidated(@PathVariable Integer id) {
        return cashAdvanceService.setAsLiquidated(id);
    }

    @Transactional(readOnly = true)
    @GetMapping("/is-document-for-cash-flow-item-assignment/{transactionId}")
    public boolean isDocumentForCashFlowItemAssignment(@PathVariable Integer transactionId) {
        return cashAdvanceService.isDocumentForCashFlowItemAssignment(transactionId);
    }

    @Transactional(readOnly = true)
    @GetMapping("/cash-flow/{caId}")
    public List<CashAdvanceBudgetDetail> getCashFlowDetails(@PathVariable Integer caId) {
        return cashAdvanceService.getCashAdvanceBudgetDetail(caId);
    }

    @PostMapping("/saveCashFlowItem")
    public PostResponse saveCashFlowItem(@RequestBody CashFlowItemDto dto, BindingResult bindingResult) {
        return cashAdvanceService.saveCashFlowItem(dto, bindingResult, messageSource);
    }

    @Transactional(readOnly = true)
    @GetMapping("/cashflow-balances/{budgetLineItemDetailId}")
    public Map<String, Object> getCashFlowBalances(@PathVariable Integer budgetLineItemDetailId) {
        int cancelled = com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId();
        BudgetLineItemDetail blid = budgetLineItemDetailRepo.findById(budgetLineItemDetailId).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        if (blid == null || blid.getCashflowItem() == null) {
            m.put("cashFlowAmountBalancePOJO", BigDecimal.ZERO);
            m.put("cashFlowAmountBalanceCV",   BigDecimal.ZERO);
            return m;
        }
        Integer cashFlowItemId = blid.getCashflowItem().getId();
        m.put("cashFlowAmountBalancePOJO", purchaseOrderBudgetDetailRepo.getCashFlowItemDetailAmountBalancePOJORFPCA(cancelled, cashFlowItemId));
        m.put("cashFlowAmountBalanceCV",   purchaseOrderBudgetDetailRepo.getCashFlowItemDetailAmountBalanceCV(cancelled, cashFlowItemId));
        return m;
    }
}
