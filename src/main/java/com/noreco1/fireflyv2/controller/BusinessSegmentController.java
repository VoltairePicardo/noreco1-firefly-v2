package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.AccountType;
import com.noreco1.fireflyv2.model.BusinessSegment;
import com.noreco1.fireflyv2.service.AccountService;
import com.noreco1.fireflyv2.service.AccountTypeService;
import com.noreco1.fireflyv2.service.BusinessSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bus-seg")
public class BusinessSegmentController {

    @Autowired
    BusinessSegmentService businessSegmentService;

    @GetMapping(value = "/list")
    
    public List<BusinessSegment> getBusinessSegments() {
        return businessSegmentService.findAll();
    }

    @GetMapping(value = "/list/fs")
    
    public List<Map> getBusinessSegmentsForFinancialStatements() {
        return businessSegmentService.findAllFs();
    }

}
