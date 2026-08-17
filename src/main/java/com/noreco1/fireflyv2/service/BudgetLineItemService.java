package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BudgetLineItem;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetLineItemService extends VoucherService {

    Page<BudgetLineItem> findAll(Pageable pageable);
    Page<BudgetLineItem> findAllByYear(Integer year, Pageable pageable);
    Page<BudgetLineItem> findAllByYearAndDivision(Integer year, Integer division, Pageable pageable);

    BudgetLineItem findById(Integer id);

    @Transactional
    PostResponse delete(Integer id);

    @Transactional
    PostResponse additionalDetail(BudgetLineItem budgetLineItem, BindingResult bindingResult, MessageSource messageSource);

    List<BudgetLineItemDetail> getBudgetLineItemDetailForRV(Boolean isFromPR);

    BigDecimal getBudgetLineItemDetailQuantityBalance(Integer budgetLineItemDetailId);
    BigDecimal getBudgetLineItemDetailAmountBalance(Integer budgetLineItemDetailId);
    BigDecimal getBudgetLineItemDetailAmountBalanceByType(Integer budgetLineItemDetailId, String type);

    List<BudgetLineItem> findAllByYearAndDivisionParams(Integer year, Integer department, Integer division, Integer status);

    Boolean isCurrentUserFsdManager();

    Boolean isCurrentUserGeneralManager();

    BudgetLineItemDetail findBudgetLineItemDetailById(Integer id);

    @Transactional
    PostResponse updateNeaApprovedAmount(BudgetLineItemDetail form, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse updateSupplementalAmount(BudgetLineItemDetail form, BindingResult bindingResult, MessageSource messageSource);
}
