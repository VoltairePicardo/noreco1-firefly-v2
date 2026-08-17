package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Section;

import java.util.List;

public interface SectionService {
    List<Section> list();
    Section findById(Integer id);
    PostResponse create(Section section);
    PostResponse update(Section section);
    PostResponse deleteById(Integer id);
}
