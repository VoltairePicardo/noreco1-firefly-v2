package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.BudgetSubItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BudgetSubItem;
import com.noreco1.fireflyv2.service.BudgetSubItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget-sub-item")
public class BudgetSubItemController {

    @Autowired
    private BudgetSubItemService budgetSubItemService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/tree")
    public List<Map> getTreeData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer divisionId,
            @RequestParam(required = false) String search) {
        return budgetSubItemService.getTreeData(year, divisionId, search);
    }

    @GetMapping("/list/{budgetLineItemDetailId}")
    public List<BudgetSubItem> list(@PathVariable Integer budgetLineItemDetailId) {
        return budgetSubItemService.getAllByBudgetLineItemDetail(budgetLineItemDetailId);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody @Valid BudgetSubItemDto dto, BindingResult bindingResult) {
        return budgetSubItemService.processCreate(dto, bindingResult, messageSource);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        PostResponse response = new PostResponse();
        try {
            response = budgetSubItemService.delete(id);
        } catch (DataIntegrityViolationException e) {
            response.setSuccess(Boolean.FALSE);
            response.setFailureMessage("Cannot delete budget sub item: It is already used in a document.");
        }
        return response;
    }

    @GetMapping("/amount-balance/{id}")
    public BigDecimal amountBalance(@PathVariable Integer id, @RequestParam String type) {
        return budgetSubItemService.getBudgetLineSubItemAmountBalanceByType(id, type);
    }
}
