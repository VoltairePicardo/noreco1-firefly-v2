package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.form.AssetVoucherLinkForm;
import com.noreco1.fireflyv2.controller.form.RetireAssetForm;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AssetService extends DataManagementService {

    Asset findByRefNo(String refNo);

    HashMap findById(Integer id);

    List<HashMap> findAll();

    List<Map> getItems(Integer assetId);

    List<Map> getDetails(Integer assetId, Integer voucherTransNo, Integer transType);

    List<AssetDepreciationSchedule> findAssetDepreciationScheduleByAssetId(Integer id);
    List<AssetVoucherLinkType> getLinkTypes();

    @Transactional
    PostResponse saveLink(AssetVoucherLinkForm form, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse retire(RetireAssetForm form, BindingResult bindingResult, MessageSource messageSource);

    Page<Asset> find(String query, Pageable pageable);

    Page<Asset> find(String query, Pageable pageable, Integer assetTypeId);

    Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, boolean isFullyDepreciated);
    Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, String status);
    Page<Asset> find(String query, Pageable pageable, Integer assetTypeId, boolean isFullyDepreciated, String status);

    Page<Asset> find(String query, Pageable pageable, boolean isFullyDepreciated);
    Page<Asset> find(String query, Pageable pageable, boolean isFullyDepreciated, String status);

    Page<Asset> find(String query, Pageable pageable, String status);

    Page<Asset> findAllForMaintenanceOrder(Pageable pageable);
    Page<Asset> findAllForMaintenanceOrderByQuery(String query, Pageable pageable);
}
