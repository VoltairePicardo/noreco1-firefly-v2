package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AccountSetting;
import com.noreco1.fireflyv2.controller.response.AccountSettingDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AccountSettingService {

    @Transactional(readOnly = true)
    List<AccountSetting> findByDateRangeAndStatusId(String from, String to);

    AccountSetting findById(Integer id);

    @Transactional
    PostResponse processCreate(AccountSetting accountSetting, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processUpdate(AccountSetting accountSetting, BindingResult bindingResult, MessageSource messageSource);

    ArrayList<AccountSettingDetailDto> getAllAccountSettingDetail(Integer transactionId);
    Map getAccountSettingRRlinkedDetail(Integer rrId);
}
