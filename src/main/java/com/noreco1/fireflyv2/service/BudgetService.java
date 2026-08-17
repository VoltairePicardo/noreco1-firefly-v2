package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Budget;
import com.noreco1.fireflyv2.model.BudgetDetail;
import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BudgetService {
    Budget findById(Integer id);

    List<Budget> findAll();

    List<Budget> getBudgetYears();

    List<BudgetDetail> findBudgetDetailByBudgetId(Integer budgetId);

    PostResponse processUpdate(Budget v, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processCreate(Budget v, BindingResult bindingResult, MessageSource messageSource);

    List<BudgetDetail> findAllBudgetDetailsByYear(Integer year);

    BigDecimal getBudgetDetailBalance(Integer budgetDetailId);

//    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

//    Map defaultSignatories();

}
