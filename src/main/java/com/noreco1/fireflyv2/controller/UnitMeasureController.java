package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.UnitMeasure;
import com.noreco1.fireflyv2.service.UnitMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit-measure")
public class UnitMeasureController {

    @Autowired
    private UnitMeasureService unitMeasureService;

    @GetMapping("/all")
    public List<UnitMeasure> listAll() {
        return unitMeasureService.listAll();
    }

    @GetMapping("/list")
    public Page<UnitMeasure> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return unitMeasureService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitMeasure> getById(@PathVariable Integer id) {
        UnitMeasure unit = unitMeasureService.findById(id);
        return unit != null ? ResponseEntity.ok(unit) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody UnitMeasure unitMeasure) {
        return unitMeasureService.create(unitMeasure);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody UnitMeasure unitMeasure) {
        return unitMeasureService.update(unitMeasure);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return unitMeasureService.deleteById(id);
    }
}
