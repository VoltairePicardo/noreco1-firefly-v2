package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.BankReconListDto;
import com.noreco1.fireflyv2.controller.response.OtherDepositDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * Created by Personal on 6/30/2015.
 */
public interface BankReconService extends DataManagementService{
    @Transactional
    public List<BankReconListDto> findAll();

    @Transactional(readOnly = true)
    public OtherDepositDto findByOdId(Integer odId);

    @Transactional
    public PostResponse process(Object entity, BindingResult bindingResult, MessageSource messageSource);
}
