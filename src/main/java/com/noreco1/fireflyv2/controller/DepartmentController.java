package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Department;
import com.noreco1.fireflyv2.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    public List<Department> list() {
        return departmentService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getById(@PathVariable Integer id) {
        Department department = departmentService.findById(id);
        return department != null ? ResponseEntity.ok(department) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Department department) {
        return departmentService.create(department);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Department department) {
        return departmentService.update(department);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return departmentService.deleteById(id);
    }
}
