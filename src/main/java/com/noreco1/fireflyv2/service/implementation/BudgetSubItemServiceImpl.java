package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.BudgetItemClassification;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.model.BudgetSubItem;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.BudgetLineItemDetailRepo;
import com.noreco1.fireflyv2.repo.BudgetSubItemRepo;
import com.noreco1.fireflyv2.controller.response.BudgetSubItemDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.BudgetSubItemService;
import com.noreco1.fireflyv2.validator.BudgetItemClassificationValidator;
import com.noreco1.fireflyv2.validator.BudgetSubItemValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BudgetSubItemServiceImpl implements BudgetSubItemService {

    private List<Map> accountsMapList;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    private BudgetSubItemRepo budgetSubItemRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getTreeData(Integer year, Integer divisionId, String searchText) {

        List<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

        try {

            if (!Checker.isValidId(year) && !Checker.isValidId(divisionId) && Checker.isStringNullOrEmpty(searchText)){
                budgetLineItemDetails = this.budgetLineItemDetailRepo.findAll();
            } else {
                String searchParam = Checker.isStringNullOrEmpty(searchText) ? null : "%" + searchText + "%";
                budgetLineItemDetails = this.budgetLineItemDetailRepo.getAllBudgetLineItemByParam(year, divisionId, searchParam);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return this.buildTreeData(budgetLineItemDetails);

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BudgetSubItem> getAllByBudgetLineItemDetail(Integer budgetLineItemDetailId) {

        List<BudgetSubItem> budgetSubItems = new ArrayList<>();

        try {

            if(Checker.isValidId(budgetLineItemDetailId)){
                budgetSubItems = this.budgetSubItemRepo.findAllByBudgetLineItemDetailId(budgetLineItemDetailId);
            } else {
                budgetSubItems = this.budgetSubItemRepo.findAll();
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return budgetSubItems;

    }

    @Override
    public PostResponse processCreate(BudgetSubItemDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            response = this.validate(dto, bindingResult, messageSource);

            if(response.isSuccess()) {

                BudgetLineItemDetail managedDetail = this.budgetLineItemDetailRepo
                        .findById(dto.getBudgetLineItemDetail().getId()).orElse(null);

                if (managedDetail == null) {
                    response.setSuccess(Boolean.FALSE);
                    response.setFailureMessage("Budget line item detail not found.");
                    return response;
                }

                List<BudgetSubItem> budgetSubItemList = new ArrayList<>();

                for (BudgetSubItem item : dto.getBudgetSubItems()){

                    BudgetSubItem budgetSubItem;

                    if(Checker.isValidId(item.getId())){
                        budgetSubItem = this.budgetSubItemRepo.findById(item.getId()).orElse(null);
                        if (budgetSubItem == null) continue;
                        budgetSubItem.setUpdatedAt(DateHelper.getServerDate());
                    } else {
                        budgetSubItem = new BudgetSubItem();
                        budgetSubItem.setCreatedAt(new Date());
                        budgetSubItem.setUpdatedAt(new Date());
                    }

                    budgetSubItem.setBudgetLineItemDetail(managedDetail);
                    budgetSubItem.setDescription(item.getDescription());
                    budgetSubItem.setAmount(item.getAmount());
                    budgetSubItem.setCreatedBy(this.authenticationFacade.getLoggedIn());

                    budgetSubItemList.add(this.budgetSubItemRepo.save(budgetSubItem));

                }

                if(Checker.collectionIsNotEmpty(budgetSubItemList)){
                    response.setSuccess(Boolean.TRUE);
                    response.setSuccessMessage("Budget sub item successfully saved.");
                } else {
                    response.setSuccess(Boolean.FALSE);
                    response.setFailureMessage("Failed to save new budget sub item.");
                }

            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse delete(Integer id) {

        PostResponse response = new PostResponse();

        BudgetSubItem foundBudgetSubItem = budgetSubItemRepo.findById(id).orElse(null);
        if(foundBudgetSubItem != null) {
            budgetSubItemRepo.delete(foundBudgetSubItem);
            response.setSuccessMessage("Budget sub item has been deleted.");
        } else {
            response.setFailureMessage("Budget sub item is not available.");
        }

        return response;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getBudgetLineSubItemAmountBalanceByType(Integer budgetSubItemDetailId, String type) {

        BigDecimal balance = BigDecimal.ZERO;

        try {

            BigDecimal getBalance;

            if(Objects.equals(type, DocumentType.CV.getCode())){
                getBalance = this.budgetSubItemRepo.getBudgetSubItemAmountBalanceCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetSubItemDetailId);
            } else {
                getBalance = this.budgetSubItemRepo.getBudgetSubItemAmountBalancePOJO(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetSubItemDetailId);
            }

            if (getBalance.compareTo(BigDecimal.ZERO) != -1){
                balance = getBalance;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return balance;

    }

    private PostResponse validate(BudgetSubItemDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BudgetSubItemValidator validator = new BudgetSubItemValidator();
        validator.setService(this);
        validator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;
    }

    private List<Map> buildTreeData(List<BudgetLineItemDetail> budgetLineItemDetails){

        this.accountsMapList = new ArrayList<>();

        try {

            for (BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails){

                Map map = new HashMap();

                map.put("id", budgetLineItemDetail.getId());
                map.put("code", budgetLineItemDetail.getCode());
                map.put("division", budgetLineItemDetail.getBudgetLineItem().getDivision().getName());
                map.put("title", budgetLineItemDetail.getTitle());
                map.put("location", budgetLineItemDetail.getLocation());
                map.put("projectCost", budgetLineItemDetail.getTotalPrice());
                map.put("isHeader", Boolean.TRUE);
                map.put("$$treeLevel", 0);

                this.accountsMapList.add(map);

                this.getSubItems(budgetLineItemDetail.getId());

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return this.accountsMapList;

    }

    private void getSubItems(Integer budgetLineItemDetailId){

        List<BudgetSubItem> budgetSubItemList = this.budgetSubItemRepo.findAllByBudgetLineItemDetailId(budgetLineItemDetailId);

        if(Checker.collectionIsNotEmpty(budgetSubItemList)){

            for (BudgetSubItem item : budgetSubItemList){

                Map map = new HashMap();

                map.put("id", item.getId());
                map.put("code", "");
                map.put("division", "");
                map.put("title", item.getDescription());
                map.put("location", "");
                map.put("projectCost", item.getAmount());
                map.put("isHeader", Boolean.FALSE);
                map.put("$$treeLevel", 1);

                this.accountsMapList.add(map);

            }

        }

    }

}
