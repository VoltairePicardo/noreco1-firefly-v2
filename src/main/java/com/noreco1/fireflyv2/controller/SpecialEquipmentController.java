package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.service.SpecialEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/special-equipment")
public class SpecialEquipmentController {

    @Autowired
    SpecialEquipmentService specialEquipmentService;

    @GetMapping(value = "/paged")
    public Page<SpecialEquipment> getSpecialEquipments(Pageable pageable,
                                                       @RequestParam(value = "q") String query) {

        return specialEquipmentService.findAllSpecialEquipmentByQuery(query, pageable);
    }

}
