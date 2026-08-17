package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Supplier;
import org.springframework.data.domain.Page;

public interface SupplierService {
    Page<Supplier> list(String q, int page, int size);
    Supplier findById(Integer id);
    PostResponse create(Supplier supplier);
    PostResponse update(Supplier supplier);
    PostResponse deleteById(Integer id);
}
