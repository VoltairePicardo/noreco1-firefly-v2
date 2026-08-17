package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.model.Factor;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface FactorService extends DataManagementService {
    Page<Factor> findAll(Pageable pageable);
    List<Factor> findAll();
    Factor findById(Integer id);
    Factor findByCode(String code);
    Page<Factor> findByQuery(String query, Pageable pageable);
    PostResponse delete(Integer factorId);
    @Transactional
    PostResponse processUpdateByValidity(Object entity, BindingResult bindingResult, MessageSource messageSource);
    boolean validityDateInUsed(Integer factorId, DateRange validityDate);
    Factor findByIdAndValidity(Integer factorId, Integer validityId);
}
