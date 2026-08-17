package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> list();
    Department findById(Integer id);
    PostResponse create(Department department);
    PostResponse update(Department department);
    PostResponse deleteById(Integer id);
}
