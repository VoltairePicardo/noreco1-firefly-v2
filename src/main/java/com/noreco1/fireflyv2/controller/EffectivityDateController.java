package com.noreco1.fireflyv2.controller;


import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.service.EffectivityDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/effect-date")
public class EffectivityDateController {

    @Autowired
    private EffectivityDateService effectivityDateService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public List<DateRange> list() {
        return effectivityDateService.findAll();
    }

    @GetMapping("/{id}")
    public DateRange getById(@PathVariable Integer id) {
        return effectivityDateService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody DateRange entity, BindingResult bindingResult) {
        return effectivityDateService.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody DateRange entity, BindingResult bindingResult) {
        return effectivityDateService.processUpdate(entity, bindingResult, messageSource);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return effectivityDateService.remove(id);
    }
}
