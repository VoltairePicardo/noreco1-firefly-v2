package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.service.CashAdvanceParticularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/cap")
public class CashAdvanceParticularController {

    @Autowired
    CashAdvanceParticularService cashAdvanceParticularService;

    @RequestMapping(value = "/list")
    
    public List<HashMap> get() {
        return cashAdvanceParticularService.findAll();
    }

    @RequestMapping(value = "/details/{id}")
    
    public List<HashMap> findByPCVId(@PathVariable Integer id) {
        return cashAdvanceParticularService.findByCAId(id);
    }

    @RequestMapping(value = "/details-for-liquidation/{id}/{calId}")
    
    public List<HashMap> findByCaIdForLiquidation(@PathVariable Integer id, @PathVariable Integer calId) {
        return cashAdvanceParticularService.findByCAIdForLiquidation(id, calId);
    }

    @GetMapping(value = "/{id}")
    
    public HashMap get(@PathVariable Integer id) {
        return cashAdvanceParticularService.findById(id);
    }
}
