package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BudgetItemClassification;
import com.noreco1.fireflyv2.service.BudgetItemClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget-item-classification")
public class BudgetItemClassificationController {

    @Autowired
    private BudgetItemClassificationService budgetItemClassificationService;

    @GetMapping("/list")
    public Page<BudgetItemClassification> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return budgetItemClassificationService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetItemClassification> getById(@PathVariable Integer id) {
        BudgetItemClassification bic = budgetItemClassificationService.findById(id);
        return bic != null ? ResponseEntity.ok(bic) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody BudgetItemClassification budgetItemClassification) {
        return budgetItemClassificationService.create(budgetItemClassification);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody BudgetItemClassification budgetItemClassification) {
        return budgetItemClassificationService.update(budgetItemClassification);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return budgetItemClassificationService.deleteById(id);
    }
}
