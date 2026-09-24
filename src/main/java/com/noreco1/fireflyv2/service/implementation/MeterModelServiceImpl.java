package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.MeterModel;
import com.noreco1.fireflyv2.mssql_repo.MssqlMeterModelRepo;
import com.noreco1.fireflyv2.repo.MeterModelRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.MeterModelService;
import com.noreco1.fireflyv2.validator.MeterModelValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterModelServiceImpl implements MeterModelService {

    private final MeterModelRepo meterModelRepo;
    private final MssqlMeterModelRepo mssqlMeterModelRepo;

    @Override
    @Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
    public PostResponse processCreate(MeterModel meterModel, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            response = this.validate(meterModel, bindingResult, messageSource);
            if (response.isSuccess()) {

                meterModel.setId(null);
                MeterModel saved = meterModelRepo.save(meterModel);

                syncMeterModelToMssql(saved);

                response.setModelId(saved.getId());
                response.setSuccessMessage("Meter Model successfully saved.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return response;
    }

    @Override
    @Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
    public PostResponse processUpdate(MeterModel meterModel, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {
            response = this.validate(meterModel, bindingResult, messageSource);

            if (response.isSuccess()) {

                MeterModel found = meterModelRepo.findById(meterModel.getId()).orElse(null);
                if (found != null) {

                    found.setBrand(meterModel.getBrand());
                    found.setMeterType(meterModel.getMeterType());
                    found.setPhase(meterModel.getPhase());
                    found.setCurrentRating(meterModel.getCurrentRating());
                    found.setAccuracyClass(meterModel.getAccuracyClass());
                    found.setMeterForm(meterModel.getMeterForm());
                    found.setModelName(meterModel.getModelName());
                    found.setConstant(meterModel.getConstant());
                    found.setAmperage(meterModel.getAmperage());
                    found.setVoltage(meterModel.getVoltage());
                    meterModelRepo.save(found);

                    syncMeterModelToMssql(found);

                    response.setModelId(found.getId());
                    response.setSuccessMessage("Meter Model successfully updated.");

                } else {
                    response.setFailureMessage("Meter Model is not available.");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return response;
    }

    private void syncMeterModelToMssql(MeterModel mysqlMeterModel) {
        if (mssqlMeterModelRepo.existsById(mysqlMeterModel.getId())) {
            return;
        }

        com.noreco1.fireflyv2.mssql_model.MeterModel mssqlMeterModel = new com.noreco1.fireflyv2.mssql_model.MeterModel();
        mssqlMeterModel.setId(mysqlMeterModel.getId());
        mssqlMeterModel.setBrand(mysqlMeterModel.getBrand() != null ? mysqlMeterModel.getBrand().getId() : null);
        mssqlMeterModel.setMeterType(mysqlMeterModel.getMeterType() != null ? mysqlMeterModel.getMeterType().getId() : null);
        mssqlMeterModel.setPhase(mysqlMeterModel.getPhase() != null ? mysqlMeterModel.getPhase().getId() : null);
        mssqlMeterModel.setCurrentRating(mysqlMeterModel.getCurrentRating() != null ? mysqlMeterModel.getCurrentRating().getId() : null);
        mssqlMeterModel.setAccuracyClass(mysqlMeterModel.getAccuracyClass() != null ? mysqlMeterModel.getAccuracyClass().getId() : null);
        mssqlMeterModel.setMeterForm(mysqlMeterModel.getMeterForm() != null ? mysqlMeterModel.getMeterForm().getId() : null);
        mssqlMeterModel.setModelName(mysqlMeterModel.getModelName());
        mssqlMeterModel.setConstant(mysqlMeterModel.getConstant());
        mssqlMeterModel.setAmperage(mysqlMeterModel.getAmperage());
        mssqlMeterModel.setVoltage(mysqlMeterModel.getVoltage());

        mssqlMeterModelRepo.save(mssqlMeterModel);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MeterModel> findAll(Pageable pageable) {
        return meterModelRepo.findByOrderByModelNameAsc(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MeterModel> find(String query, Pageable pageable) {
        return meterModelRepo.findByModelNameContainingIgnoreCaseOrderByModelNameAsc(query.trim(), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public MeterModel findById(Integer id) {
        return meterModelRepo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public MeterModel findByModelName(String modelName) {
        return meterModelRepo.findTop1ByModelNameIgnoreCase(modelName.trim());
    }

    @Transactional(readOnly = true)
    @Override
    public List<MeterModel> findAll() {
        return meterModelRepo.findByOrderByModelNameAsc(Pageable.unpaged()).getContent();
    }

    private PostResponse validate(MeterModel meterModel, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        MeterModelValidator validator = new MeterModelValidator();
        validator.setService(this);
        validator.validate(meterModel, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;
    }
}
