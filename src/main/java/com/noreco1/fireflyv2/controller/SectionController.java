package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Section;
import com.noreco1.fireflyv2.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @GetMapping("/list")
    public List<Section> list() {
        return sectionService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Section> getById(@PathVariable Integer id) {
        Section section = sectionService.findById(id);
        return section != null ? ResponseEntity.ok(section) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Section section) {
        return sectionService.create(section);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Section section) {
        return sectionService.update(section);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return sectionService.deleteById(id);
    }
}
