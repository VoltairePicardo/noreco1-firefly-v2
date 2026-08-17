package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BankDeposit;
import com.noreco1.fireflyv2.controller.response.BankDepositDto;
import com.noreco1.fireflyv2.controller.response.BankDepositListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.UploadBankDepositDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface BankDepositService {

    @Transactional
    public PostResponse processUpdate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processCreate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processUpdate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);

    @Transactional
    public PostResponse processCreate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    public List<BankDepositListDto> findAll();

    @Transactional(readOnly = true)
    public BankDepositDto findById(Integer id);

    @Transactional
    PostResponse uploadDeposits(UploadBankDepositDto uploadBankDepositDto, BindingResult bindingResult, MessageSource messageSource);

}
