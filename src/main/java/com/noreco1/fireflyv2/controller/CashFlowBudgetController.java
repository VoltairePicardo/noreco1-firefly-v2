package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.Budget;
import com.noreco1.fireflyv2.model.BudgetDetail;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-flow-budget")
public class CashFlowBudgetController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("budgetServiceImpl")
    BudgetService budgetService;

    @GetMapping("/list")
    public List<Budget> list() {
        return budgetService.findAll();
    }

    @GetMapping("/years")
    public List<Budget> years() {
        return budgetService.getBudgetYears();
    }

    @GetMapping("/{id}")
    public Budget getById(@PathVariable Integer id) {
        return budgetService.findById(id);
    }

    @GetMapping("/details/{id}")
    public List<BudgetDetail> details(@PathVariable Integer id) {
        return budgetService.findBudgetDetailByBudgetId(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Budget budget) {
        BindingResult bindingResult = new BeanPropertyBindingResult(budget, "budget");
        return budgetService.processCreate(budget, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Budget budget) {
        BindingResult bindingResult = new BeanPropertyBindingResult(budget, "budget");
        return budgetService.processUpdate(budget, bindingResult, messageSource);
    }
}
