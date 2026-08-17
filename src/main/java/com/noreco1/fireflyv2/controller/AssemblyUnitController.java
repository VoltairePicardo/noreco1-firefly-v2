package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.AssemblyType;
import com.noreco1.fireflyv2.model.AssemblyUnit;
import com.noreco1.fireflyv2.service.AssemblyUnitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assembly-unit")
public class AssemblyUnitController {

    @Autowired
    private AssemblyUnitService assemblyUnitService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public Page<AssemblyUnit> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return assemblyUnitService.findAll(PageRequest.of(page, size), q.isEmpty() ? null : q);
    }

    @GetMapping("/{id}")
    public AssemblyUnit getById(@PathVariable Integer id) {
        return assemblyUnitService.findById(id);
    }

    @GetMapping("/types")
    public List<AssemblyType> types() {
        return assemblyUnitService.findAllAssemblyType();
    }

    @GetMapping("/types/{id}")
    public AssemblyType getTypeById(@PathVariable Integer id) {
        return assemblyUnitService.findAssemblyType(id);
    }

    @PostMapping("/types/create")
    public PostResponse createType(@Valid @RequestBody AssemblyType entity, BindingResult bindingResult) {
        return assemblyUnitService.createAssemblyType(entity, bindingResult, messageSource);
    }

    @PostMapping("/types/update")
    public PostResponse updateType(@Valid @RequestBody AssemblyType entity, BindingResult bindingResult) {
        return assemblyUnitService.createAssemblyType(entity, bindingResult, messageSource);
    }

    @PostMapping("/create")
    public PostResponse create(@Valid @RequestBody AssemblyUnit entity, BindingResult bindingResult) {
        return assemblyUnitService.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@Valid @RequestBody AssemblyUnit entity, BindingResult bindingResult) {
        return assemblyUnitService.processUpdate(entity, bindingResult, messageSource);
    }
}
