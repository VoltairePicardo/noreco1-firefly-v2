package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.AccountGroup;
import com.noreco1.fireflyv2.model.AccountType;
import com.noreco1.fireflyv2.service.AccountGroupService;
import com.noreco1.fireflyv2.service.AccountTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/account/type")
public class AccountTypeController {

    @Autowired
    AccountTypeService accountTypeService;

    @GetMapping(value = "/list")
    
    public List<AccountType> getAccountTypes() {
        return accountTypeService.findAll();
    }
}
