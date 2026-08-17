package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.DateRangeRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.EffectivityDateService;
import com.noreco1.fireflyv2.validator.EffectivityDateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yer on 8/8/2016.
 */

@Service
public class EffectivityDateServiceImpl implements EffectivityDateService {

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {
            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            DateRange dateRange = (DateRange) entity;

            EffectivityDateValidator validator = new EffectivityDateValidator();
            validator.setService(this);
            validator.validate(dateRange, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Error found");
            } else {

                if (dateRange.getDescription() == null || dateRange.getDescription().trim().length() == 0) {
                    dateRange.setDescription(DateHelper.dateToLongDate(dateRange.getStart()) + " - " + DateHelper.dateToLongDate(dateRange.getEnd()));
                }
                if (dateRange.getId() != null && dateRange.getId() == 0) {
                    dateRange.setId(null);
                }
                dateRange = dateRangeRepo.save(dateRange);

                response.setModelId(dateRange.getId());
                response.setSuccessMessage("Effectivity date successfully saved");
            }

        }catch (Exception e) {
            response.setFailureMessage("Something went wrong!");
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public List<DateRange> findByStartRange(Date start, Date end) {
        List<DateRange> dateRanges = new ArrayList<>();
        try {
            java.sql.Date startSql = new java.sql.Date(start.getTime());
            java.sql.Date endSql = new java.sql.Date(end.getTime());

            dateRanges =  dateRangeRepo.findByStartBetween(startSql, endSql);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return dateRanges;
    }

    @Override
    public List<DateRange> findByEndRange(Date start, Date end) {
        List<DateRange> dateRanges = new ArrayList<>();
        try {
            java.sql.Date startSql = new java.sql.Date(start.getTime());
            java.sql.Date endSql = new java.sql.Date(end.getTime());

            dateRanges =  dateRangeRepo.findByEndBetween(startSql, endSql);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return dateRanges;
    }

    @Override
    public List<DateRange> findOverlapping(Date start, Date end) {
        List<DateRange> dateRanges = new ArrayList<>();
        try {
            java.sql.Date startSql = new java.sql.Date(start.getTime());
            java.sql.Date endSql   = new java.sql.Date(end.getTime());
            dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(startSql, endSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateRanges;
    }

    @Override
    public DateRange findById(Integer id) {
        return dateRangeRepo.findById(id).orElse(null);
    }

    @Override
    public List<DateRange> findAll() {
        return dateRangeRepo.findAllByOrderByEndDescStartDesc();
    }

    @Transactional
    @Override
    public PostResponse remove(Integer id) {
        PostResponse response = new PostResponse();

        try {

            DateRange range = dateRangeRepo.findById(id).orElse(null);
            if(range != null) {
                dateRangeRepo.delete(range);
                response.setSuccessMessage("Effectivity date successfully deleted");
            } else {
                response.setFailureMessage("Data is not available");
            }

        }catch (Exception e) {
            response.setFailureMessage(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
