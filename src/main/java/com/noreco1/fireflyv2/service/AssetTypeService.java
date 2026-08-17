package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AssetType;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface AssetTypeService {
    PostResponse processCreate(AssetType assetType, BindingResult bindingResult, MessageSource messageSource);

    PostResponse delete(Integer id);

    PostResponse processUpdate(AssetType assetType, BindingResult bindingResult, MessageSource messageSource);

    Page<AssetType> findAll(Pageable pageable);
    Page<AssetType> find(String query, Pageable pageable);
    AssetType findById(Integer id);
    AssetType findByDescription(String query);
    List<AssetType> findAll();
}
