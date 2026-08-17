package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface VoucherService extends DocumentService {

    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    List<DocumentStatus> getDocumentsStatuses();
}
