package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.MeterModel;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface MeterModelService {
    PostResponse processCreate(MeterModel meterModel, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processUpdate(MeterModel meterModel, BindingResult bindingResult, MessageSource messageSource);

    Page<MeterModel> findAll(Pageable pageable);
    Page<MeterModel> find(String query, Pageable pageable);
    MeterModel findById(Integer id);
    MeterModel findByModelName(String modelName);
    List<MeterModel> findAll();
}
