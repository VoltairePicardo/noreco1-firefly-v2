package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Canvass;
import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.controller.response.CanvassDto;
import com.noreco1.fireflyv2.controller.response.CanvassListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 5/12/2015.
 */
public interface CanvassService extends VoucherService {
    public Canvass findByCode(String code);

    @Transactional(readOnly = true)
    public CanvassDto findById(Integer canvassId);

    @Transactional
    public List<CanvassListDto> findAll();

    @Transactional(readOnly = true)
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<Map> findByDateRangePending(String from, String to);

    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    Map quotationDefaultSignatories();
}
