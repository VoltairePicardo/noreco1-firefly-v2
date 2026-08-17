package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Division;

import java.util.List;

public interface DivisionService {
    List<Division> list();
    Division findById(Integer id);
    PostResponse create(Division division);
    PostResponse update(Division division);
    PostResponse deleteById(Integer id);
}
