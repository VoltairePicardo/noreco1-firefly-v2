package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.UnitMeasure;
import com.noreco1.fireflyv2.repo.UnitMeasureRepo;
import com.noreco1.fireflyv2.service.UnitMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitMeasureServiceImpl implements UnitMeasureService {

    @Autowired
    private UnitMeasureRepo unitMeasureRepo;

    @Override
    public List<UnitMeasure> listAll() {
        return unitMeasureRepo.findAllByOrderByCodeAsc();
    }

    @Override
    public Page<UnitMeasure> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return q.isBlank()
                ? unitMeasureRepo.findAllByOrderByCodeAsc(pageable)
                : unitMeasureRepo.findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByCodeAsc(q, q, pageable);
    }

    @Override
    public UnitMeasure findById(Integer id) {
        return unitMeasureRepo.findById(id).orElse(null);
    }

    @Override
    public UnitMeasure findByCode(String code) {
        return unitMeasureRepo.findOneByCodeIgnoreCase(code.trim());
    }

    @Override
    public UnitMeasure findByDescription(String description) {
        return unitMeasureRepo.findOneByDescriptionIgnoreCase(description.trim());
    }

    @Override
    @Transactional
    public PostResponse create(UnitMeasure unitMeasure) {
        unitMeasure.setId(null);
        PostResponse res = new PostResponse();
        try {
            UnitMeasure saved = unitMeasureRepo.save(unitMeasure);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Unit of Measure successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(UnitMeasure unitMeasure) {
        PostResponse res = new PostResponse();
        try {
            unitMeasureRepo.save(unitMeasure);
            res.setModelId(unitMeasure.getId());
            res.setSuccessMessage("Unit of Measure successfully updated!");
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
            unitMeasureRepo.deleteById(id);
            res.setSuccessMessage("Unit of Measure successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
