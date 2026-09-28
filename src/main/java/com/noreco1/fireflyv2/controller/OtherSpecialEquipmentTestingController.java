package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.OtherSpecialEquipmentTesting;
import com.noreco1.fireflyv2.service.OtherSpecialEquipmentTestingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/other-special-equipment-testing")
@RequiredArgsConstructor
public class OtherSpecialEquipmentTestingController {

    private final OtherSpecialEquipmentTestingService otherSpecialEquipmentTestingService;
    private final MessageSource messageSource;

    @GetMapping(value = "/{id}")
    public OtherSpecialEquipmentTesting getById(@PathVariable Integer id) {
        return otherSpecialEquipmentTestingService.getById(id);
    }

    @GetMapping(value = "/list")
    public Page<OtherSpecialEquipmentTesting> getAll(@RequestParam(defaultValue = "") String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return otherSpecialEquipmentTestingService.findAll(pageable);
        }

        return otherSpecialEquipmentTestingService.findAllByQuery(query, pageable);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody OtherSpecialEquipmentTesting otherSpecialEquipmentTesting, BindingResult bindingResult) {
        return otherSpecialEquipmentTestingService.create(otherSpecialEquipmentTesting, bindingResult, messageSource);
    }

}
