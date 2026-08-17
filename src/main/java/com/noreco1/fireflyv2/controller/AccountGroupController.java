package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.AccountGroup;
import com.noreco1.fireflyv2.service.AccountGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/group")
public class AccountGroupController {

    @Autowired
    AccountGroupService accountGroupService;

    @GetMapping(value = "/list")
    
    public List<AccountGroup> getAccountGroups() {
        return accountGroupService.findAll();
    }

}
