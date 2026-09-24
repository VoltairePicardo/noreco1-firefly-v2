package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MeterModel;
import com.noreco1.fireflyv2.service.MeterModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/meter-model")
public class MeterModelController {

    @Autowired
    private MeterModelService meterModelService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<MeterModel> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return meterModelService.findAll(pageable);
        }
        return meterModelService.find(q, pageable);
    }

    @GetMapping("/all")
    public List<MeterModel> listAll() {
        return meterModelService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeterModel> getById(@PathVariable Integer id) {
        MeterModel meterModel = meterModelService.findById(id);
        return meterModel != null ? ResponseEntity.ok(meterModel) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MeterModel meterModel) {
        BindingResult bindingResult = new BeanPropertyBindingResult(meterModel, "meterModel");
        return meterModelService.processCreate(meterModel, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody MeterModel meterModel) {
        BindingResult bindingResult = new BeanPropertyBindingResult(meterModel, "meterModel");
        return meterModelService.processUpdate(meterModel, bindingResult, messageSource);
    }
}
