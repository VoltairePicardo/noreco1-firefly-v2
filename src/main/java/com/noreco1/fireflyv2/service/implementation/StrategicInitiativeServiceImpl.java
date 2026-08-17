package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.StrategicInitiative;
import com.noreco1.fireflyv2.repo.StrategicInitiativeRepo;
import com.noreco1.fireflyv2.service.StrategicInitiativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StrategicInitiativeServiceImpl implements StrategicInitiativeService {

    @Autowired
    private StrategicInitiativeRepo strategicInitiativeRepo;

    @Override
    public Page<StrategicInitiative> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        return q.isBlank()
                ? strategicInitiativeRepo.findAll(pageable)
                : strategicInitiativeRepo.findByDescriptionContainingIgnoreCase(q, pageable);
    }

    @Override
    public List<StrategicInitiative> listAll() {
        return strategicInitiativeRepo.findByOrderByDescriptionAsc();
    }

    @Override
    public StrategicInitiative findById(Integer id) {
        return strategicInitiativeRepo.findById(id).orElse(null);
    }

    @Override
    public StrategicInitiative findByDepartmentIdAndDescription(Integer departmentId, String description) {
        return strategicInitiativeRepo.findByDepartmentIdAndDescriptionContainingIgnoreCase(departmentId, description.trim());
    }

    @Override
    @Transactional
    public PostResponse create(StrategicInitiative strategicInitiative) {
        strategicInitiative.setId(null);
        PostResponse res = new PostResponse();
        try {
            StrategicInitiative saved = strategicInitiativeRepo.save(strategicInitiative);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Strategic initiative successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(StrategicInitiative strategicInitiative) {
        PostResponse res = new PostResponse();
        try {
            StrategicInitiative existing = strategicInitiativeRepo.findById(strategicInitiative.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Strategic initiative not found."); return res; }
            existing.setDescription(strategicInitiative.getDescription());
            existing.setDepartment(strategicInitiative.getDepartment());
            strategicInitiativeRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Strategic initiative successfully updated!");
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
            strategicInitiativeRepo.deleteById(id);
            res.setSuccessMessage("Strategic initiative successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
