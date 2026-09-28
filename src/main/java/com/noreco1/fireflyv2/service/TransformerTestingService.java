package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.TransformerTesting;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

public interface TransformerTestingService {

    TransformerTesting getById(Integer id);
    Page<TransformerTesting> findAll(Pageable pageable);
    Page<TransformerTesting> findAllByQuery(String query, Pageable pageable);


    PostResponse create(TransformerTesting transformerTesting, BindingResult bindingResult, MessageSource messageSource);
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

}
