package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MiscellaneousCharge;
import com.noreco1.fireflyv2.service.MiscellaneousChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Created by tonyc on 1/29/2020.
 */
@RestController
@RequestMapping(value = "/api/misc-charge")
public class MiscellaneousChargeController {

    @Autowired
    private MiscellaneousChargeService miscellaneousChargeService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<MiscellaneousCharge> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return miscellaneousChargeService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MiscellaneousCharge> getById(@PathVariable Integer id) {
        MiscellaneousCharge miscellaneousCharge = miscellaneousChargeService.findOne(id);
        return miscellaneousCharge != null ? ResponseEntity.ok(miscellaneousCharge) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MiscellaneousCharge miscellaneousCharge) {
        BindingResult bindingResult = new BeanPropertyBindingResult(miscellaneousCharge, "miscellaneousCharge");
        return miscellaneousChargeService.processCreate(miscellaneousCharge, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody MiscellaneousCharge miscellaneousCharge) {
        BindingResult bindingResult = new BeanPropertyBindingResult(miscellaneousCharge, "miscellaneousCharge");
        return miscellaneousChargeService.processUpdate(miscellaneousCharge, bindingResult, messageSource);
    }
}
