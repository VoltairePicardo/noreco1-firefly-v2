package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Brand;
import org.springframework.data.domain.Page;

public interface BrandService {
    Page<Brand> list(String q, int page, int size);
    Brand findById(Integer id);
    PostResponse create(Brand brand);
    PostResponse update(Brand brand);
    PostResponse deleteById(Integer id);
}
