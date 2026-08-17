package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CheckConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/check")
public class CheckController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CheckConfigService checkConfigService;

    @PostMapping(value = "/update")
    
    public PostResponse updateCheckConfig(@Valid @RequestBody CheckConfig config, BindingResult bindingResult) {
        return checkConfigService.processUpdate(config, bindingResult, messageSource);
    }

    @PostMapping(value = "/create")
    
    public PostResponse createCheckConfig(@Valid @RequestBody CheckConfig config, BindingResult bindingResult) {
        return checkConfigService.processCreate(config, bindingResult, messageSource);
    }

    @GetMapping(value = "/{id}")
    
    public CheckConfig getCheck(@PathVariable Integer id) {
        return checkConfigService.findById(id);
    }

    @GetMapping(value = "/list")
    
    public List<CheckConfig> getCheckConfigs() {
        return checkConfigService.findAll();
    }
}
