package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BudgetItemClassification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BudgetItemClassificationService {
    Page<BudgetItemClassification> list(String q, int page, int size);
    List<BudgetItemClassification> listAll();
    BudgetItemClassification findById(Integer id);
    BudgetItemClassification findByDescription(String description);
    PostResponse create(BudgetItemClassification budgetItemClassification);
    PostResponse update(BudgetItemClassification budgetItemClassification);
    PostResponse deleteById(Integer id);
}
