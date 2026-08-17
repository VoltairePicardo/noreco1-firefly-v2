package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.AllocationFactorDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AllocationFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/allocation-factor")
public class AllocationFactorController {

    @Autowired
    private AllocationFactorService allocationFactorService;

    @Autowired
    MessageSource messageSource;

    @GetMapping(value = "/list")
    public List<AllocationFactorDto> list() {
        return allocationFactorService.findAll();
    }

    @GetMapping(value = "/date-ranges")
    public List<Map> dateRanges() {
        return allocationFactorService.findDateRanges();
    }

    @GetMapping(value = "/by-effectivity/{effDateId}")
    public List<AllocationFactorDto> byEffectivity(@PathVariable Integer effDateId) {
        return allocationFactorService.findByEffectivityDateId(effDateId);
    }

    @GetMapping(value = "/by-account-and-effectivity")
    public AllocationFactorDto byAccountAndEffectivity(@RequestParam Integer accountId, @RequestParam Integer effId) {
        return allocationFactorService.findByAccountAndEffectivityId(accountId, effId);
    }

    @PostMapping(value = "/create")
    public PostResponse create(@RequestBody Object entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "allocationFactor");
        return allocationFactorService.processCreate(entity, bindingResult, messageSource);
    }

}
