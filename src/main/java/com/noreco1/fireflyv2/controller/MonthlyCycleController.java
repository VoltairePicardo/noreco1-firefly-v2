package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MonthlyCycle;
import com.noreco1.fireflyv2.service.MonthlyCycleClosingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monthly-closing")
public class MonthlyCycleController {

    @Autowired
    MonthlyCycleClosingService service;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public List<MonthlyCycle> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MonthlyCycle getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/logs")
    public List<Map> getLogs(@PathVariable Integer id) {
        return service.findLogs(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MonthlyCycle entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "monthlyCycle");
        return service.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody MonthlyCycle entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "monthlyCycle");
        return service.processUpdate(entity, bindingResult, messageSource);
    }
}
