package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.GeneralClassification;
import com.noreco1.fireflyv2.repo.GeneralClassificationRepo;
import com.noreco1.fireflyv2.service.GeneralClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeneralClassificationServiceImpl implements GeneralClassificationService {

    @Autowired
    private GeneralClassificationRepo generalClassificationRepo;

    @Override
    public Page<GeneralClassification> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        return q.isBlank()
                ? generalClassificationRepo.findAll(pageable)
                : generalClassificationRepo.findByDescriptionContainingIgnoreCase(q, pageable);
    }

    @Override
    public List<GeneralClassification> listAll() {
        return generalClassificationRepo.findByOrderByDescriptionAsc();
    }

    @Override
    public GeneralClassification findById(Integer id) {
        return generalClassificationRepo.findById(id).orElse(null);
    }

    @Override
    public GeneralClassification findByDescription(String description) {
        return generalClassificationRepo.findByDescriptionContainingIgnoreCase(description.trim());
    }

    @Override
    @Transactional
    public PostResponse create(GeneralClassification generalClassification) {
        generalClassification.setId(null);
        PostResponse res = new PostResponse();
        try {
            GeneralClassification saved = generalClassificationRepo.save(generalClassification);
            res.setModelId(saved.getId());
            res.setSuccessMessage("General classification successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(GeneralClassification generalClassification) {
        PostResponse res = new PostResponse();
        try {
            GeneralClassification existing = generalClassificationRepo.findById(generalClassification.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("General classification not found."); return res; }
            existing.setDescription(generalClassification.getDescription());
            generalClassificationRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("General classification successfully updated!");
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
            generalClassificationRepo.deleteById(id);
            res.setSuccessMessage("General classification successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
