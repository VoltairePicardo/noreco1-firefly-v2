package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Section;
import com.noreco1.fireflyv2.repo.SectionRepo;
import com.noreco1.fireflyv2.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepo sectionRepo;

    @Override
    public List<Section> list() {
        return sectionRepo.findAll();
    }

    @Override
    public Section findById(Integer id) {
        return sectionRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Section section) {
        section.setId(null);
        PostResponse res = new PostResponse();
        try {
            Section saved = sectionRepo.save(section);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Section successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Section section) {
        PostResponse res = new PostResponse();
        try {
            sectionRepo.save(section);
            res.setModelId(section.getId());
            res.setSuccessMessage("Section successfully updated!");
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
            sectionRepo.deleteById(id);
            res.setSuccessMessage("Section successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
