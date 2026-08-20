package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.MiscellaneousCharge;
import com.noreco1.fireflyv2.repo.MiscellaneousChargeRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.MiscellaneousChargeService;
import com.noreco1.fireflyv2.validator.MiscellaneousChargeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tonyc on 1/29/2020.
 */
@Service
public class MiscellaneousChargeServiceImpl implements MiscellaneousChargeService {

    @Autowired
    MiscellaneousChargeRepo miscellaneousChargeRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    private MiscellaneousCharge model;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        MiscellaneousCharge miscellaneousCharge = miscellaneousChargeRepo.findById(id).orElse(null);

        if (miscellaneousCharge != null) {
            map = composeHashMap(miscellaneousCharge);
        }

        return map;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public MiscellaneousCharge findOne(Integer id) {
        return miscellaneousChargeRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();
        List<MiscellaneousCharge> miscellaneousCharges = miscellaneousChargeRepo.findAll();

        if (!Checker.collectionIsEmpty(miscellaneousCharges)) {
            for (MiscellaneousCharge pcf : miscellaneousCharges) {
                mapList.add(composeHashMap(pcf));
            }
        }

        return mapList;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MiscellaneousCharge> findAll(Pageable pageable) {
        return miscellaneousChargeRepo.findAll(pageable);
    }

    private HashMap composeHashMap(MiscellaneousCharge miscellaneousCharge) {
        HashMap<String, Object> hm = new HashMap<>();

        try {
            hm.put("id", miscellaneousCharge.getId());
            hm.put("account", miscellaneousCharge.getAccount());
            hm.put("description", miscellaneousCharge.getDescription());
            hm.put("amount", miscellaneousCharge.getAmount());
            hm.put("vatable", miscellaneousCharge.getVatable());
        } catch (Exception ex) {
            Logger.getLogger(MiscellaneousChargeServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
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
            MiscellaneousCharge miscellaneousCharge = (MiscellaneousCharge) entity;
            MiscellaneousChargeValidator validator = new MiscellaneousChargeValidator();
            validator.setService(this);
            validator.validate(miscellaneousCharge, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Please check required fields.");
            } else {

                boolean insert = !Checker.isValidId(miscellaneousCharge.getId());

                if(insert) {
                    miscellaneousCharge.setId(null);
                    this.model = miscellaneousChargeRepo.save(miscellaneousCharge);
                } else {
                    MiscellaneousCharge exMC = miscellaneousChargeRepo.findById(miscellaneousCharge.getId()).orElse(null);
                    if(exMC != null) {

                        exMC.setDescription(miscellaneousCharge.getDescription());
                        exMC.setAccount(miscellaneousCharge.getAccount());
                        exMC.setAmount(miscellaneousCharge.getAmount());
                        exMC.setVatable(miscellaneousCharge.getVatable());

                        this.model = miscellaneousChargeRepo.save(exMC);
                    } else {
                        response.setFailureMessage("Miscellaneous Charge is not available.");
                    }
                }

                if(this.model != null) {
                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("Miscellaneous Charge successfully saved.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
