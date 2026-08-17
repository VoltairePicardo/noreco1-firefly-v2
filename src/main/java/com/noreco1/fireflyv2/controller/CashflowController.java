package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.reports.CashFlowStatementDetail;
import com.noreco1.fireflyv2.dtoers.AccountingReportDtoer;
import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.CashflowItemLog;
import com.noreco1.fireflyv2.model.CashflowItemType;
import com.noreco1.fireflyv2.service.CashflowItemService;
import com.noreco1.fireflyv2.service.CashflowItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/cashflow-item")
public class CashflowController {

    @Autowired
    private CashflowItemService cashflowItemService;

    @Autowired
    private CashflowItemTypeService cashflowItemTypeService;

    @Autowired
    private AccountingReportDtoer accountingReportDtoer;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/statement/{from}/{to}")
    public List<CashFlowStatementDetail> cashFlowStatement(@PathVariable String from, @PathVariable String to) {
        try {
            int yearTo = DateHelper.toYear(to);
            int yearFrom = DateHelper.toYear(from);
            if (yearFrom == yearTo)
                return accountingReportDtoer.getForCashFlowStatement(from, to);
            return new java.util.ArrayList<>();
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. from=" + from + ", to=" + to, e
            );
        }
    }

    @GetMapping("/statement-bsup/{from}/{to}")
    public List<CashFlowStatementDetail> cashFlowStatementBsup(@PathVariable String from, @PathVariable String to) {
        try{int yearTo   = DateHelper.toYear(to);
        int yearFrom = DateHelper.toYear(from);
        if (yearFrom == yearTo)
            return accountingReportDtoer.getForCashFlowStatementBSUP(from, to);
        return new java.util.ArrayList<>(); } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. from=" + from + ", to=" + to, e
            );
        }
    }

    @GetMapping("/list")
    public List<CashflowItem> list() {
        return cashflowItemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashflowItem> getById(@PathVariable Integer id) {
        CashflowItem cashflowItem = cashflowItemService.findById(id);
        return cashflowItem != null ? ResponseEntity.ok(cashflowItem) : ResponseEntity.notFound().build();
    }

    @GetMapping("/types")
    public List<CashflowItemType> types() {
        return cashflowItemTypeService.findAll();
    }

    @GetMapping("/{id}/logs")
    public List<CashflowItemLog> logs(@PathVariable Integer id) {
        return cashflowItemService.getLogs(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody CashflowItem cashflowItem) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cashflowItem, "cashflowItem");
        return cashflowItemService.processCreate(cashflowItem, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody CashflowItem cashflowItem) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cashflowItem, "cashflowItem");
        return cashflowItemService.processUpdate(cashflowItem, bindingResult, messageSource);
    }
}
