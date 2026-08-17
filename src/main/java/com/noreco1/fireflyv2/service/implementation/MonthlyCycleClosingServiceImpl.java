package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.MonthlyCycle;
import com.noreco1.fireflyv2.model.MonthlyCycleLog;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.enums.MonthlyCycleStatus;
import com.noreco1.fireflyv2.repo.MonthlyCycleLogRepo;
import com.noreco1.fireflyv2.repo.MonthlyCycleRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.service.MonthlyCycleClosingService;
import com.noreco1.fireflyv2.validator.AccountValidator;
import com.noreco1.fireflyv2.validator.MonthlyCycleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MonthlyCycleClosingServiceImpl implements MonthlyCycleClosingService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    MonthlyCycleLogRepo monthlyCycleLogRepo;

    @Transactional
    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        MonthlyCycle mc = (MonthlyCycle) entity;
        return this.processCreate(mc, bindingResult, messageSource);
    }

    @Transactional
    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        MonthlyCycle mc = (MonthlyCycle) entity;

        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            MonthlyCycleValidator validator = new MonthlyCycleValidator();
            validator.setMonthlyCycleRepo(monthlyCycleRepo);
            validator.validate(mc, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setSuccess(false);
            } else {
                User createdBy = authenticationFacade.getLoggedIn();
                Boolean insertMode = mc.getId() == null;
                MonthlyCycle existingMonthlyCycle;

                if (!insertMode) {
                    existingMonthlyCycle = monthlyCycleRepo.findById(mc.getId()).orElse(null);
                    if (existingMonthlyCycle != null) { // update is legit
                        existingMonthlyCycle.setStatus(mc.getStatus());
                        existingMonthlyCycle.setYear(mc.getYear());
                        existingMonthlyCycle.setMonth(mc.getMonth());
                        existingMonthlyCycle.setUpdatedAt(new java.util.Date());
                    } else {
                        response.setSuccessMessage("Monthly Cycle not found");
                        return response;
                    }
                } else {
                    existingMonthlyCycle = new MonthlyCycle();
                    existingMonthlyCycle.setStatus(mc.getStatus());
                    existingMonthlyCycle.setYear(mc.getYear());
                    existingMonthlyCycle.setMonth(mc.getMonth());
                    existingMonthlyCycle.setCreatedAt(new java.util.Date());
                    existingMonthlyCycle.setUpdatedAt(new java.util.Date());
                }

                MonthlyCycle newMonthlyCycle = monthlyCycleRepo.save(existingMonthlyCycle);
                if (newMonthlyCycle != null) {
                    // logging
                    MonthlyCycleLog log = new MonthlyCycleLog();

                    log.setStatus(newMonthlyCycle.getStatus());
                    log.setYear(newMonthlyCycle.getYear());
                    log.setMonth(newMonthlyCycle.getMonth());
                    log.setMonthlyCycle(newMonthlyCycle);
                    log.setCreatedBy(createdBy);
                    log.setCreatedAt(new Date());

                    monthlyCycleLogRepo.save(log);

                    response.setSuccess(true);
                    response.setModelId(newMonthlyCycle.getId());
                    if (newMonthlyCycle.getStatus() == MonthlyCycleStatus.CLOSE) {
                        response.setSuccessMessage("Monthly Cycle successfully closed!");
                    } else {
                        response.setSuccessMessage("Monthly Cycle successfully created!");
                    }
                }
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return response;
    }

    @Override
    public List<MonthlyCycle> findAll() {
        return monthlyCycleRepo.findAllByOrderByYearDescMonthDesc();
    }

    @Override
    public MonthlyCycle findById(Integer id) {
        return monthlyCycleRepo.findById(id).orElse(null);
    }

    @Override
    public List<Map> findLogs(Integer id) {
        List<Map> data = new ArrayList<>();

        List<MonthlyCycleLog> logs = monthlyCycleLogRepo.findAllByMonthlyCycleIdOrderByCreatedAtDesc(id);
        if (!Checker.collectionIsEmpty(logs)) {
            for(MonthlyCycleLog log:logs) {
                Map m  = new HashMap();
                m.put("datetime", log.getCreatedAt());
                m.put("by", log.getCreatedBy() != null ? log.getCreatedBy().getFullName() : "");
                m.put("status", log.getStatus());
                m.put("month", StringFormatter.getMonthFromNumber(log.getMonth()-1));
                m.put("year", log.getYear());

                data.add(m);
            }
        }

        return data;
    }

    @Override
    public Map findByYearAndMonth(Integer year, Integer month) {

        Map m  = new HashMap();

        try {

            MonthlyCycle cycle = monthlyCycleRepo.findByYearAndMonth(year, month);

            if (cycle != null) {

                m.put("id", cycle.getId());
                m.put("year", cycle.getYear());
                m.put("month", cycle.getMonth());
                m.put("status", cycle.getStatus());
            }

        } catch (Exception ex) {
            Logger.getLogger(MonthlyCycleClosingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return m;
    }
}
