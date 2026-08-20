package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.BudgetType;
import com.noreco1.fireflyv2.repo.BudgetTypeRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.BudgetTypeService;
import com.noreco1.fireflyv2.validator.BudgetTypeValidator;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class BudgetTypeServiceImpl implements BudgetTypeService {

    @Autowired
    private BudgetTypeRepo budgetTypeRepo;

    @Override
    public PostResponse processCreate(BudgetType budgetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            response = this.validate(budgetType, bindingResult, messageSource);
            if(response.isSuccess()) {

                BudgetType savedType = budgetTypeRepo.save(budgetType);

                if(savedType != null) {
                    response.setModelId(savedType.getId());
                    response.setSuccessMessage("Budget type successfully saved.");
                } else {
                    response.setFailureMessage("Failed to save new Asset Type.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public PostResponse processUpdate(BudgetType budgetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {
            response = this.validate(budgetType, bindingResult, messageSource);

            if(response.isSuccess()) {

                BudgetType foundBudgetType = budgetTypeRepo.findById(budgetType.getId()).orElse(null);
                if(foundBudgetType != null) {

                    foundBudgetType.setDescription(budgetType.getDescription());
                    budgetTypeRepo.save(foundBudgetType);

                    response.setModelId(foundBudgetType.getId());
                    response.setSuccessMessage("Budget type successfully updated.");

                } else {
                    response.setFailureMessage("Budget type is not available.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<BudgetType> findAll(Pageable pageable) {
        return budgetTypeRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<BudgetType> find(String query, Pageable pageable) {
        query = "%"+query.trim()+"%";
        return budgetTypeRepo.findByDescriptionContainingIgnoreCase(query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BudgetType findById(Integer id) {
        return budgetTypeRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BudgetType findByDescription(String query) {
        return budgetTypeRepo.findByDescriptionContainingIgnoreCase(query.trim());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BudgetType> findAll() {
        return budgetTypeRepo.findByOrderByDescriptionAsc();
    }

    private PostResponse validate(BudgetType budgetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BudgetTypeValidator validator = new BudgetTypeValidator();
        validator.setService(this);
        validator.validate(budgetType, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;
    }

}
