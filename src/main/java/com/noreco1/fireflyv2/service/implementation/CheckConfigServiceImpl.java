package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.repo.CheckConfigRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PostRoleResponse;
import com.noreco1.fireflyv2.service.CheckConfigService;
import com.noreco1.fireflyv2.validator.CheckConfigValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CheckConfigServiceImpl implements CheckConfigService {

    @Autowired
    CheckConfigRepo checkConfigRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CheckConfig> findAll() {
        try {
            return checkConfigRepo.findAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckConfig findById(Integer id) {
        return checkConfigRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CheckConfig findByCode(String code) {
        return checkConfigRepo.findOneByCode(code);
    }

    @Override
    @Transactional
    public PostResponse processUpdate(CheckConfig config, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();

        CheckConfig existing = checkConfigRepo.findById(config.getId()).orElse(null);
        if (existing == null) { response.setFailureMessage("Check config not found."); return response; }

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        CheckConfigValidator validator = new CheckConfigValidator();
        validator.setService(this);
        validator.validate(config, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {
            existing.setCode(config.getCode());
            existing.setBankAccount(config.getBankAccount());
            existing.setDateFormat(config.getDateFormat());
            existing.setCheckNoPrefix(config.getCheckNoPrefix());
            existing.setDateX(config.getDateX());
            existing.setDateY(config.getDateY());
            existing.setPayeeX(config.getPayeeX());
            existing.setPayeeY(config.getPayeeY());
            existing.setPayeeW(config.getPayeeW());
            existing.setNumericAmountX(config.getNumericAmountX());
            existing.setNumericAmountY(config.getNumericAmountY());
            existing.setAlphaAmountX(config.getAlphaAmountX());
            existing.setAlphaAmountY(config.getAlphaAmountY());
            existing.setAlphaAmountW(config.getAlphaAmountW());
            existing.setSig1X(config.getSig1X());
            existing.setSig1Y(config.getSig1Y());
            existing.setSig2X(config.getSig2X());
            existing.setSig2Y(config.getSig2Y());
            existing.setCheckNoX(config.getCheckNoX());
            existing.setCheckNoY(config.getCheckNoY());
            existing.setDateLineSpacing(config.getDateLineSpacing());
            existing.setAmountNoOfCharToAdjust(config.getAmountNoOfCharToAdjust());
            existing.setNameNoOfCharToAdjust(config.getNameNoOfCharToAdjust());
            existing.setWithSigner(config.getWithSigner());
            existing.setShowDesignation(config.getShowDesignation());
            existing.setUpdatedAt(new Date());

            CheckConfig saved = checkConfigRepo.save(existing);
            response.setModelId(saved.getId());
            response.setSuccessMessage("Check config successfully updated!");
            response.setSuccess(true);
        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processCreate(CheckConfig config, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostRoleResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CheckConfigValidator validator = new CheckConfigValidator();
        validator.setService(this);
        validator.validate(config, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {
            if (config.getId() != null && config.getId() == 0) config.setId(null);
            config.setUpdatedAt(new Date());
            CheckConfig newCheck = checkConfigRepo.save(config);

            response.setModelId(newCheck.getId());
            response.setSuccessMessage("Check config successfully saved!");
            response.setSuccess(true);
        }
        return response;
    }
}
