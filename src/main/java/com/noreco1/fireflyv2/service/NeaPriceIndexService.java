package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.NeaPriceIndex;
import com.noreco1.fireflyv2.model.NeaPriceIndexDetail;
import org.springframework.data.domain.Page;

public interface NeaPriceIndexService {
    Page<NeaPriceIndex> list(String q, int page, int size);
    NeaPriceIndex findById(Integer id);
    NeaPriceIndex findByIdWithPrice(Integer id);
    NeaPriceIndexDetail getItemNeaPriceIndex(Integer itemId);
    PostResponse create(NeaPriceIndex neaPriceIndex);
    PostResponse update(NeaPriceIndex neaPriceIndex);
    PostResponse deleteById(Integer id);
}
