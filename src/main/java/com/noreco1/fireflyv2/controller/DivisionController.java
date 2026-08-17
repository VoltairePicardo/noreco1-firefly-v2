package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Division;
import com.noreco1.fireflyv2.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/division")
public class DivisionController {

    @Autowired
    private DivisionService divisionService;

    @GetMapping("/list")
    public List<Division> list() {
        return divisionService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Division> getById(@PathVariable Integer id) {
        Division division = divisionService.findById(id);
        return division != null ? ResponseEntity.ok(division) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Division division) {
        return divisionService.create(division);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Division division) {
        return divisionService.update(division);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return divisionService.deleteById(id);
    }
}
