package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.TransformerTesting;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.HashMap;

public interface TransformerTestingService {

    TransformerTesting getById(Integer id);
    Page<TransformerTesting> findAll(Pageable pageable);
    Page<TransformerTesting> findAllByQuery(String query, Pageable pageable);

    HashMap<String, Object> transformerTestingParameters(HttpServletRequest request, Integer id);
    JRDataSource datasourceTransformerTesting(Integer id);

    PostResponse create(TransformerTesting transformerTesting, BindingResult bindingResult, MessageSource messageSource);
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

}
