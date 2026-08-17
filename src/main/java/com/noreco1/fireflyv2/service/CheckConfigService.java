package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface CheckConfigService {
    public List<CheckConfig> findAll();
    public CheckConfig findById(Integer id);
    public CheckConfig findByCode(String code);
    public PostResponse processUpdate(CheckConfig config, BindingResult bindingResult, MessageSource messageSource);
    public PostResponse processCreate(CheckConfig config, BindingResult bindingResult, MessageSource messageSource);
}
