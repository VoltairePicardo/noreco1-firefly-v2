package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.FactorFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.service.FactorService;
import com.noreco1.fireflyv2.validator.FactorValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FactorServiceImpl implements FactorService {

    @Autowired
    FactorRepo factorRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    private FactorFacade factorFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Factor> findAll(Pageable pageable) {
        return factorRepo.findAllByOrderByCodeAsc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Factor> findAll() {
        return factorRepo.findAllByOrderByCodeAsc();
    }

    public boolean validityDateInUsed(Integer factorId, DateRange validityDate) {

        Set<FactorPercentageDistro> distros = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(factorId, validityDate.getId());
        return Checker.collectionIsNotEmpty(distros);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Factor findByIdAndValidity(Integer factorId, Integer validityId) {
        Factor factor = factorRepo.findById(factorId).orElse(null);

        if(factor != null) {

            factor.setValidityDate(dateRangeRepo.findById(validityId).orElse(null));

            Set<FactorPercentageDistro> distros = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(factor.getId(), validityId);
            factor.setFactorPercentageDistroSet(distros);
        }
        return factor;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Factor findById(Integer id) {
        Factor factor = factorRepo.findById(id).orElse(null);

        if(factor != null) {

            List<AllocationFactor> allocationFactors = allocationFactorRepo.findByFactorId(factor.getId());
            factor.setRemovable(Checker.collectionIsEmpty(allocationFactors));

            factor.setFactorPercentageDistroSetByValidity(factorFacade.getPercentageDistrosByValidity(factor.getId()));

        }

        return factor;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Factor findByCode(String code) {
        code = code.trim().toUpperCase();
        return factorRepo.findOneByCode(code);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Factor> findByQuery(String query, Pageable pageable) {
        return factorRepo.findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByDescriptionAsc(query, query, pageable);
    }

    @Override
    @Transactional
    public PostResponse delete(Integer factorId) {

        PostResponse response = new PostRoleResponse();

        Factor factor = factorRepo.findById(factorId).orElse(null);

        if(factor != null) {

            List<AllocationFactor> allocationFactors = allocationFactorRepo.findByFactorId(factorId);
            if(Checker.collectionIsNotEmpty(allocationFactors)) {

                response.setFailureMessage("You can't delete factor currently allocated to an account.");

            } else  {

                factorPercentageDistroRepo.deleteByFactorId(factorId);
                factorRepo.deleteById(factorId);

                response.setSuccessMessage("Factor has been deleted.");
            }

        } else  {
            response.setFailureMessage("Factor is not available.");
        }

        return response;
    }

    @Override
    public PostResponse processUpdateByValidity(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        Factor factor = (Factor) entity;

        PostResponse response = new PostRoleResponse();

        try {

            BigDecimal total = BigDecimal.ZERO;
            for (FactorPercentageDistro distro : factor.getFactorPercentageDistroSet()) {

                if (distro.getPercentage() != null) {
                    total = total.add(distro.getPercentage());
                }
            }

            if (total.compareTo(new BigDecimal("100")) == 0) {

                Factor foundFactor = factorRepo.findById(factor.getId()).orElse(null);

                if(foundFactor != null && factor.getValidityDate() != null) {

                    // reset
                    factorPercentageDistroRepo.deleteByFactorIdAndValidityDateId(foundFactor.getId(), factor.getValidityDate().getId());

                    this.saveFactorPercentageDistro(factor.getFactorPercentageDistroSet(), factor);

                    response.setSuccessMessage("Percentage distributions have been updated.");
                }
            } else {
                response.setFailureMessage("Percentage total should be 100%");
            }

        }catch (Exception e) {
            response.setFailureMessage("Something went wrong!");
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        Factor factor = (Factor) entity;

        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        FactorValidator validator = new FactorValidator();
        validator.setService(this);
        validator.validate(factor, bindingResult);

        if (bindingResult.hasErrors()) {

            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);

        } else {

            Factor newFactor;

            if (factor.getId() == null || factor.getId() == 0) {    // insert

                factor.setCode(factor.getCode().toUpperCase());
                newFactor = factorRepo.save(factor);
                newFactor.setValidityDate(factor.getValidityDate());

                this.saveFactorPercentageDistro(factor.getFactorPercentageDistroSet(), newFactor);

                response.setModelId(newFactor.getId());
                response.setSuccessMessage("Factor has been saved.");

            } else {    // update

                Factor exFactor = factorRepo.findById(factor.getId()).orElse(null);
                if(exFactor == null) {

                    response.setFailureMessage("Factor is not available");

                } else {

                    exFactor.setCode(factor.getCode().toUpperCase());
                    exFactor.setDescription(factor.getDescription());
                    exFactor.setUpdatedAt(new Date());

                    factorRepo.save(exFactor);

                    if(factor.getValidityDate() != null) {

                        exFactor.setValidityDate(factor.getValidityDate());
                        this.saveFactorPercentageDistro(factor.getFactorPercentageDistroSet(), exFactor);
                    }

                    response.setModelId(exFactor.getId());
                    response.setSuccessMessage("Factor has been updated.");
                }
            }
        }

        return response;
    }

    private void saveFactorPercentageDistro(Set<FactorPercentageDistro> percentageDistroSet, Factor factor) {

        for (FactorPercentageDistro distro: percentageDistroSet) {

            distro.setFactor(factor);
            distro.setValidityDate(factor.getValidityDate());
            distro.setPercentage(distro.getPercentage().divide(new BigDecimal(100)));

            factorPercentageDistroRepo.save(distro);

        }
    }
}
