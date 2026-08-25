package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BudgetItemClassification;
import com.noreco1.fireflyv2.repo.BudgetItemClassificationRepo;
import com.noreco1.fireflyv2.service.BudgetItemClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BudgetItemClassificationImpl implements BudgetItemClassificationService {

    @Autowired
    private BudgetItemClassificationRepo budgetItemClassificationRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<BudgetItemClassification> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        return q.isBlank()
                ? budgetItemClassificationRepo.findAll(pageable)
                : budgetItemClassificationRepo.findByDescriptionContainingIgnoreCase(q, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BudgetItemClassification> listAll() {
        return budgetItemClassificationRepo.findByOrderByDescriptionAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BudgetItemClassification findById(Integer id) {
        return budgetItemClassificationRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BudgetItemClassification findByDescription(String description) {
        return budgetItemClassificationRepo.findByDescriptionContainingIgnoreCase(description.trim());
    }

    @Override
    @Transactional
    public PostResponse create(BudgetItemClassification budgetItemClassification) {
        budgetItemClassification.setId(null);
        PostResponse res = new PostResponse();
        try {
            BudgetItemClassification saved = budgetItemClassificationRepo.save(budgetItemClassification);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Budget item classification successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(BudgetItemClassification budgetItemClassification) {
        PostResponse res = new PostResponse();
        try {
            BudgetItemClassification existing = budgetItemClassificationRepo.findById(budgetItemClassification.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Budget item classification not found."); return res; }
            existing.setDescription(budgetItemClassification.getDescription());
            budgetItemClassificationRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Budget item classification successfully updated!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public PostResponse deleteById(Integer id) {
        PostResponse res = new PostResponse();
        try {
            budgetItemClassificationRepo.deleteById(id);
            res.setSuccessMessage("Budget item classification successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
