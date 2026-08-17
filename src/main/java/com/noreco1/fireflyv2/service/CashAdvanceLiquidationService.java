package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CashAdvanceLiquidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI on 5/27/2023.
 */
public interface CashAdvanceLiquidationService extends VoucherService {

    CashAdvanceLiquidation findById(Integer id);

    List<HashMap> findAll();

    @Transactional(readOnly = true)
    List<HashMap> findByStatusId(Integer id);

    @Transactional(readOnly = true)
    List<HashMap> findByDateRangeAndStatusId(String from, String to, Integer id, Integer officeId);

    @Transactional(readOnly = true)
    List<HashMap> findByDateRange(String from, String to, Integer officeId);

    Page<CashAdvanceLiquidation> findAllForJv(String query, Pageable pageable);

}
