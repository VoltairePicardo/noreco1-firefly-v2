package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.PettyCashTransDetail;
import com.noreco1.fireflyv2.service.PettyCashTransDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/pctd")
public class PettyCashTransDetailController {

    @Autowired
    PettyCashTransDetailService pettyCashTransDetailService;

    @RequestMapping(value = "/list")
    
    public List<HashMap> get() {
        return pettyCashTransDetailService.findAll();
    }

    @RequestMapping(value = "/details/{id}")
    
    public List<PettyCashTransDetail> findByPCVId(@PathVariable Integer id) {
        return pettyCashTransDetailService.findByPCVId(id);
    }

    @GetMapping(value = "/{id}")
    
    public HashMap get(@PathVariable Integer id) {
        return pettyCashTransDetailService.findById(id);
    }
}
