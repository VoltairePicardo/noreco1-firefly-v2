package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

public interface DataManagementService {
    @Transactional
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource);
}
