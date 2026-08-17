package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.FundingSource;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FundingSourceService {
    Page<FundingSource> list(String q, int page, int size);
    List<FundingSource> listAll();
    FundingSource findById(Integer id);
    FundingSource findByDescription(String description);
    PostResponse create(FundingSource fundingSource);
    PostResponse update(FundingSource fundingSource);
    PostResponse deleteById(Integer id);
}
