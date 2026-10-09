package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.BudgetLineItem;
import com.noreco1.fireflyv2.service.BudgetLineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget-line-item")
public class BudgetLineItemController {

    @Autowired
    @Qualifier("budgetLineItemServiceImpl")
    BudgetLineItemService budgetLineItemService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public List<BudgetLineItem> list(@RequestParam(value = "y",   required = false) Integer year,
                                     @RequestParam(value = "dep", required = false) Integer department,
                                     @RequestParam(value = "div", required = false) Integer division,
                                     @RequestParam(value = "s",   required = false) Integer status) {
        return budgetLineItemService.findAllByYearAndDivisionParams(year, department, division, status);
    }

    @GetMapping("/document-statuses")
    public List<com.noreco1.fireflyv2.model.DocumentStatus> documentStatuses() {
        return budgetLineItemService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return budgetLineItemService.defaultSignatories();
    }

    // type = CV | CA | PCL | PCV | POJO; omitted = PO/JO/RFP balance (as used by Payment Request)
    @GetMapping("/detail-amount-balance/{id}")
    public BigDecimal detailAmountBalance(@PathVariable Integer id, @RequestParam(value = "type", required = false) String type) {
        return type == null
                ? budgetLineItemService.getBudgetLineItemDetailAmountBalance(id)
                : budgetLineItemService.getBudgetLineItemDetailAmountBalanceByType(id, type);
    }

    @GetMapping("/{id}")
    public BudgetLineItem getData(@PathVariable Integer id) {
        return budgetLineItemService.findById(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart("model") @Valid BudgetLineItem budgetLineItem,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = budgetLineItemService.processCreate(budgetLineItem, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            budgetLineItemService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart("model") @Valid BudgetLineItem budgetLineItem,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = budgetLineItemService.processUpdate(budgetLineItem, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            budgetLineItemService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return budgetLineItemService.process(postData, bindingResult, messageSource);
    }

    @DeleteMapping("/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return budgetLineItemService.delete(id);
    }
}
