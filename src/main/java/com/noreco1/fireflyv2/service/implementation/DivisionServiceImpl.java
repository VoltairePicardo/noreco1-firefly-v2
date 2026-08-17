package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Division;
import com.noreco1.fireflyv2.repo.DivisionRepo;
import com.noreco1.fireflyv2.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DivisionServiceImpl implements DivisionService {

    @Autowired
    private DivisionRepo divisionRepo;

    @Override
    public List<Division> list() {
        return divisionRepo.findAllByOrderByNameAsc();
    }

    @Override
    public Division findById(Integer id) {
        return divisionRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Division division) {
        division.setId(null);
        PostResponse res = new PostResponse();
        try {
            Division saved = divisionRepo.save(division);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Division successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Division division) {
        PostResponse res = new PostResponse();
        try {
            divisionRepo.save(division);
            res.setModelId(division.getId());
            res.setSuccessMessage("Division successfully updated!");
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
            divisionRepo.deleteById(id);
            res.setSuccessMessage("Division successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
