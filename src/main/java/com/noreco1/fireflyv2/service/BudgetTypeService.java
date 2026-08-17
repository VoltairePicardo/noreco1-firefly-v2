package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BudgetType;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface BudgetTypeService {

    @Transactional
    PostResponse processCreate(BudgetType budgetType, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processUpdate(BudgetType budgetType, BindingResult bindingResult, MessageSource messageSource);

    Page<BudgetType> findAll(Pageable pageable);
    Page<BudgetType> find(String query, Pageable pageable);
    BudgetType findById(Integer id);
    BudgetType findByDescription(String query);
    List<BudgetType> findAll();

}
