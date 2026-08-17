package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.UnitMeasure;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UnitMeasureService {
    Page<UnitMeasure> list(String q, int page, int size);
    List<UnitMeasure> listAll();
    UnitMeasure findById(Integer id);
    UnitMeasure findByCode(String code);
    UnitMeasure findByDescription(String description);
    PostResponse create(UnitMeasure unitMeasure);
    PostResponse update(UnitMeasure unitMeasure);
    PostResponse deleteById(Integer id);
}
