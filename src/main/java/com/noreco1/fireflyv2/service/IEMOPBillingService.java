package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.IEMOPBilling;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by TSI on 10/20/2023.
 */
public interface IEMOPBillingService {
    IEMOPBilling findById(Integer id);
    Page<IEMOPBilling> findAllForListing(String query, Pageable pageable);
    Page<IEMOPBilling> findAllForCVPaged(String query, Pageable pageable);
    List<IEMOPBilling> findAllForCV(String query);
    @Transactional
    PostResponse processUpload(MultipartFile excelFile, String date, String refNumber);

}
