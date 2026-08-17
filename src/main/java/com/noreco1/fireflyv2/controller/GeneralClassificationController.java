package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.GeneralClassification;
import com.noreco1.fireflyv2.service.GeneralClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/general-classification")
public class GeneralClassificationController {

    @Autowired
    private GeneralClassificationService generalClassificationService;

    @GetMapping("/list")
    public Page<GeneralClassification> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return generalClassificationService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralClassification> getById(@PathVariable Integer id) {
        GeneralClassification gc = generalClassificationService.findById(id);
        return gc != null ? ResponseEntity.ok(gc) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody GeneralClassification generalClassification) {
        return generalClassificationService.create(generalClassification);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody GeneralClassification generalClassification) {
        return generalClassificationService.update(generalClassification);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return generalClassificationService.deleteById(id);
    }
}
