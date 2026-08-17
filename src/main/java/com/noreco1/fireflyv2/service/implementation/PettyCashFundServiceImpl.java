package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.PettyCashFund;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.repo.PettyCashFundRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.PettyCashFundService;
import com.noreco1.fireflyv2.validator.PettyCashFundValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PettyCashFundServiceImpl implements PettyCashFundService {

    @Autowired
    PettyCashFundRepo pettyCashFundRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    private PettyCashFund model;

    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        PettyCashFund pettyCashFund = pettyCashFundRepo.findById(id).orElse(null);

        if (pettyCashFund != null) {
            map = composeHashMap(pettyCashFund);
        }

        return map;
    }

    @Override
    public PettyCashFund findOne(Integer id) {
        return pettyCashFundRepo.findById(id).orElse(null);
    }

    @Override
    public PettyCashFund findByOfficeId(Integer officeId) {
        return pettyCashFundRepo.findOneByOfficeId(officeId);
    }

    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();
        List<PettyCashFund> pettyCashFunds = pettyCashFundRepo.findAll();

        if (!Checker.collectionIsEmpty(pettyCashFunds)) {
            for (PettyCashFund pcf : pettyCashFunds) {
                mapList.add(composeHashMap(pcf));
            }
        }

        return mapList;
    }

    @Override
    public Page<PettyCashFund> findAll(Pageable pageable) {
        return pettyCashFundRepo.findAll(pageable);
    }

    @Override
    public Page<PettyCashFund> find(String query, Pageable pageable) {
        query = "%"+query.trim()+"%";
        return pettyCashFundRepo.findByDescriptionContainingIgnoreCase(query, pageable);
    }

    private HashMap composeHashMap(PettyCashFund pettyCashFund) {
        HashMap<String, Object> hm = new HashMap<>();

        try {
            hm.put("id", pettyCashFund.getId());
            hm.put("accountNo", pettyCashFund.getAccountNo());
            hm.put("description", pettyCashFund.getDescription());
            hm.put("balance", pettyCashFund.getBalance());
        } catch (Exception ex) {
            Logger.getLogger(PettyCashFundServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            PettyCashFund pettyCashFund = (PettyCashFund) entity;

            PettyCashFundValidator validator = new PettyCashFundValidator();
            validator.setService(this);
            validator.validate(pettyCashFund, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Please check required fields.");
            } else {

                boolean insert = !Checker.isValidId(pettyCashFund.getId());

                if(insert) {

                    pettyCashFund.setId(null);
                    SLEntityClassification slEntityClassification = new SLEntityClassification();
                    slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.PETTY_CASH.getId());

                    pettyCashFund.setSlEntityClassification(slEntityClassification);
                    pettyCashFund.setAccountNo(generatorFacade.entityAccountNumber());
                    pettyCashFund.setCreatedAt(new Date());
                    pettyCashFund.setUpdatedAt(new Date());

                    this.model = pettyCashFundRepo.save(pettyCashFund);
                    
                } else {

                    PettyCashFund exCashFund = pettyCashFundRepo.findById(pettyCashFund.getId()).orElse(null);
                    if(exCashFund != null) {

                        exCashFund.setDescription(pettyCashFund.getDescription());
                        exCashFund.setOffice(pettyCashFund.getOffice());
                        exCashFund.setAccount(pettyCashFund.getAccount());
                        exCashFund.setUpdatedAt(new Date());

                        this.model = pettyCashFundRepo.save(exCashFund);
                    } else {
                        response.setFailureMessage("Petty cash fund is not available.");
                    }
                }

                if(this.model != null) {

                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("Petty cash fund successfully saved.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
