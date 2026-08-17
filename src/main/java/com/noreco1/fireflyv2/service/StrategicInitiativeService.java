package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.StrategicInitiative;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StrategicInitiativeService {
    Page<StrategicInitiative> list(String q, int page, int size);
    List<StrategicInitiative> listAll();
    StrategicInitiative findById(Integer id);
    StrategicInitiative findByDepartmentIdAndDescription(Integer departmentId, String description);
    PostResponse create(StrategicInitiative strategicInitiative);
    PostResponse update(StrategicInitiative strategicInitiative);
    PostResponse deleteById(Integer id);
}
