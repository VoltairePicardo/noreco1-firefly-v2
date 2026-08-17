package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.SubSupplier;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by TSI on 10/18/2023.
 */
public interface SubSupplierService {

    SubSupplier findById(Integer id);
    List<SubSupplier> findAllBySupplier(Integer suppId);
    Page<SubSupplier> findAllForListing(String query, Integer suppId, Pageable pageable);
    @Transactional
    PostResponse processUpload(MultipartFile excelFile, Integer supplierId);

}
