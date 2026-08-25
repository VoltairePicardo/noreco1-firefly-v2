package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Department;
import com.noreco1.fireflyv2.repo.DepartmentRepo;
import com.noreco1.fireflyv2.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepo departmentRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Department> list() {
        return departmentRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Department findById(Integer id) {
        return departmentRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Department department) {
        department.setId(null);
        PostResponse res = new PostResponse();
        try {
            Department saved = departmentRepo.save(department);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Department successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Department department) {
        PostResponse res = new PostResponse();
        try {
            departmentRepo.save(department);
            res.setModelId(department.getId());
            res.setSuccessMessage("Department successfully updated!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public PostResponse deleteById(Integer id) {
        PostResponse res = new PostResponse();
        try {
            departmentRepo.deleteById(id);
            res.setSuccessMessage("Department successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
