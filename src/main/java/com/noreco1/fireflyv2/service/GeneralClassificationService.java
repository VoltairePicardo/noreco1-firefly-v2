package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.GeneralClassification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GeneralClassificationService {
    Page<GeneralClassification> list(String q, int page, int size);
    List<GeneralClassification> listAll();
    GeneralClassification findById(Integer id);
    GeneralClassification findByDescription(String description);
    PostResponse create(GeneralClassification generalClassification);
    PostResponse update(GeneralClassification generalClassification);
    PostResponse deleteById(Integer id);
}
