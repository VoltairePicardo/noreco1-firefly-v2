package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BudgetSubItem;
import com.noreco1.fireflyv2.controller.response.BudgetSubItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BudgetSubItemService {

    List<Map> getTreeData(Integer year, Integer divisionId, String searchText);

    List<BudgetSubItem> getAllByBudgetLineItemDetail(Integer budgetLineItemDetailId);

    @Transactional
    PostResponse processCreate(BudgetSubItemDto dto, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse delete(Integer id);

    BigDecimal getBudgetLineSubItemAmountBalanceByType(Integer budgetSubItemDetailId, String type);

}
