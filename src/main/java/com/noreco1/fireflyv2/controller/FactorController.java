package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Factor;
import com.noreco1.fireflyv2.service.FactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/factor")
public class FactorController {

    @Autowired
    private FactorService factorService;

    @Autowired
    MessageSource messageSource;

    @GetMapping(value = "/list")
    public Page<Factor> list(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        if (q == null || q.trim().isEmpty()) {
            return factorService.findAll(PageRequest.of(page, size));
        }
        return factorService.findByQuery(q, PageRequest.of(page, size));
    }

    @GetMapping(value = "/all")
    public List<Factor> listAll() {
        return factorService.findAll();
    }

    @GetMapping(value = "/{id}")
    public Factor findById(@PathVariable Integer id) {
        return factorService.findById(id);
    }

    @PostMapping(value = "/create")
    public PostResponse create(@RequestBody Factor entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "factor");
        return factorService.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping(value = "/update")
    public PostResponse update(@RequestBody Factor entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "factor");
        return factorService.processUpdate(entity, bindingResult, messageSource);
    }

    @PostMapping(value = "/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return factorService.delete(id);
    }

    @GetMapping(value = "/{factorId}/validity/{validityId}")
    public Factor findByIdAndValidity(@PathVariable Integer factorId, @PathVariable Integer validityId) {
        return factorService.findByIdAndValidity(factorId, validityId);
    }

    @PostMapping(value = "/update-by-validity")
    public PostResponse updateByValidity(@RequestBody Factor entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "factor");
        return factorService.processUpdateByValidity(entity, bindingResult, messageSource);
    }

}
