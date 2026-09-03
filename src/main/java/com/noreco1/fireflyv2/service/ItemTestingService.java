package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.model.PoDetail;
import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
public interface ItemTestingService {

    Page<ItemTesting> findAll(String startDate, String endDate, Pageable pageable);

    Page<Map<String, Object>> getItemTestingPaged(String from, String to, Pageable pageable);

    @Transactional
    PostResponse update(ItemTesting itemTesting, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse create(ItemTesting itemTesting, BindingResult bindingResult, MessageSource messageSource);

    ItemTestingDto findById(Integer id);

    PostResponse delete(Integer id);

    @Transactional(readOnly = true)
    List<Map> getItemTestingDetails(Integer id);

    List<InventoryLocation> getInventoryLocations();

    List<PoDetail> getPurchaseOrderDetailsForItemTesting(Integer poId);

}
