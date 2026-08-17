package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.CashflowItemType;
import com.noreco1.fireflyv2.service.CashflowItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RestController
@RequestMapping(value = "/cashflow-item-type")
public class CashflowItemTypeController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CashflowItemTypeService cashflowItemTypeService;

    @RequestMapping(value = "/list")
    
    public List<CashflowItemType> get() {
        return cashflowItemTypeService.findAll();
    }

    @GetMapping(value = "/{id}")
    
    public CashflowItemType get(@PathVariable Integer id) {
        return cashflowItemTypeService.findById(id);
    }
}
