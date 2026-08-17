package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AssetDepreciation;
import com.noreco1.fireflyv2.model.AssetDepreciationDetail;
import com.noreco1.fireflyv2.model.form.YearMonth;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface AssetDepreciationService extends DataManagementService {

    @Transactional
    PostResponse process(YearMonth yearMonth, BindingResult bindingResult, MessageSource messageSource);

    List<AssetDepreciation> findAllByYearAndMonth(Integer year, Integer month);
    AssetDepreciation findById(Integer id);
    List<Map> findAllDetailsById(Integer id);
    Map getTotals(Integer assetDepreciationId);
}
