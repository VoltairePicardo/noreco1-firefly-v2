package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.CashflowItemLoggerFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.CashflowItemLog;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.CashflowItemRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CashflowItemService;
import com.noreco1.fireflyv2.validator.CashflowAccountValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.*;

@Service
public class CashflowItemServiceImpl implements CashflowItemService {

    @Autowired
    CashflowItemRepo cashflowItemRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    CashflowItemLoggerFacade cashflowItemLoggerFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CashflowItem> findAll() {
        return cashflowItemRepo.findAllByOrderByIdAscNameAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CashflowItem> findByIdNotIn() {
        List<CashflowItem> cashflowItems = cashflowItemRepo.findByParentCashflowItemNotNull();
        ArrayList<Integer> ids = new ArrayList<>();

        for (CashflowItem cfi : cashflowItems) {
            ids.add(cfi.getParentCashflowItem().getId());
        }

        return cashflowItemRepo.findByIdNotIn(ids);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CashflowItem findById(Integer id) {
        return cashflowItemRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CashflowItemLog> getLogs(Integer id) {
        return cashflowItemLoggerFacade.getLogs(id);
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        CashflowItem cashflowItem = (CashflowItem) entity;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        CashflowAccountValidator validator = new CashflowAccountValidator();

        validator.validate(cashflowItem, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();

            response = messageFormatter.getResponse();

            response.setSuccess(false);
        } else {

            User loggedIn = authenticationFacade.getLoggedIn();
            CashflowItem cfa;

            boolean updating = Checker.isValidId(cashflowItem.getId());

            if(updating) {

                cfa = cashflowItemRepo.findById(cashflowItem.getId()).orElse(null);

                if(cfa != null) {

                    cfa.setModifiedBy(loggedIn);
                    cfa.setUpdatedAt(new Date());
                    cfa.setName(cashflowItem.getName());
                    cfa.setOrdinalNumber(cashflowItem.getOrdinalNumber());
                    cfa.setCashflowItemType(cashflowItem.getCashflowItemType());
                    cfa.setParentCashflowItem(cashflowItem.getParentCashflowItem());
                    cfa.setAccount(cashflowItem.getAccount());
                    cfa.setShowInCashFlowStatement(cashflowItem.getShowInCashFlowStatement());

                    cfa = cashflowItemRepo.save(cfa);
                }

            } else {

                cashflowItem.setId(null);
                cashflowItem.setCreatedAt(new Date());
                cashflowItem.setUpdatedAt(new Date());
                cashflowItem.setModifiedBy(loggedIn);
                cashflowItem.setCreatedBy(loggedIn);

                cfa = cashflowItemRepo.save(cashflowItem);

            }

            if (cfa != null) {

                cashflowItemLoggerFacade.log(cfa);

                response.setModelId(cfa.getId());
                response.setSuccessMessage("Cashflow Account successfully saved!");

                response.setSuccess(true);
            }

        }

        return response;
    }
}
