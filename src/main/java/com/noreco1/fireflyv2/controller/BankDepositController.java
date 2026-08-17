package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BankDeposit;
import com.noreco1.fireflyv2.service.BankDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-deposit")
public class BankDepositController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("bankDepositServiceImpl")
    BankDepositService service;

    @GetMapping("/list")
    public List<?> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody BankDeposit entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "bankDeposit");
        return service.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody BankDeposit entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "bankDeposit");
        return service.processUpdate(entity, bindingResult, messageSource);
    }
}
