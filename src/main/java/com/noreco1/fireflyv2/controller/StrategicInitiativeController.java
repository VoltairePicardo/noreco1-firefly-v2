package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.StrategicInitiative;
import com.noreco1.fireflyv2.service.StrategicInitiativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/strategic-initiative")
public class StrategicInitiativeController {

    @Autowired
    private StrategicInitiativeService strategicInitiativeService;

    @GetMapping("/list")
    public Page<StrategicInitiative> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return strategicInitiativeService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StrategicInitiative> getById(@PathVariable Integer id) {
        StrategicInitiative si = strategicInitiativeService.findById(id);
        return si != null ? ResponseEntity.ok(si) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody StrategicInitiative strategicInitiative) {
        return strategicInitiativeService.create(strategicInitiative);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody StrategicInitiative strategicInitiative) {
        return strategicInitiativeService.update(strategicInitiative);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return strategicInitiativeService.deleteById(id);
    }
}
