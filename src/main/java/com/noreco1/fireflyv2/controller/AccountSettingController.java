package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.AccountSetting;
import com.noreco1.fireflyv2.controller.response.AccountSettingDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AccountSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/account-setting")
public class AccountSettingController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    private AccountSettingService accountSettingService;

    @GetMapping(value = "/list/{from}/{to}")
    public List<AccountSetting> list(@PathVariable String from, @PathVariable String to) {
        return accountSettingService.findByDateRangeAndStatusId(from, to);
    }

    @GetMapping(value = "/{id}")
    public AccountSetting getById(@PathVariable Integer id) {
        return accountSettingService.findById(id);
    }

    @PostMapping(value = "/create")
    public PostResponse create(@Valid @RequestBody AccountSetting accountSetting, BindingResult bindingResult) {
        return accountSettingService.processCreate(accountSetting, bindingResult, messageSource);
    }

    @PostMapping(value = "/update")
    public PostResponse update(@Valid @RequestBody AccountSetting accountSetting, BindingResult bindingResult) {
        return accountSettingService.processUpdate(accountSetting, bindingResult, messageSource);
    }

    @GetMapping(value = "/detail/{transactionId}")
    public List<AccountSettingDetailDto> getDetail(@PathVariable Integer transactionId, HttpServletRequest request) {
        return accountSettingService.getAllAccountSettingDetail(transactionId);
    }

    @GetMapping(value = "/rr-linked-details/{rrId}")
    public Map getRrLinkedDetails(@PathVariable Integer rrId, HttpServletRequest request) {
        return accountSettingService.getAccountSettingRRlinkedDetail(rrId);
    }
}
