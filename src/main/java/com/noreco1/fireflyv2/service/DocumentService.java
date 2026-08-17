package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Map;

public interface DocumentService {
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public void logNewValue(Integer logId);

    @Transactional(readOnly = true)
    public Map defaultSignatories();

}
