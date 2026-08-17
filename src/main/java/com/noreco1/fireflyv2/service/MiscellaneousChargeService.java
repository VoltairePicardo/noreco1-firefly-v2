package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.MiscellaneousCharge;
import com.noreco1.fireflyv2.model.PettyCashFund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tonyc on 1/29/2020.
 */
public interface MiscellaneousChargeService extends DataManagementService {
    HashMap findById(Integer id);
    MiscellaneousCharge findOne(Integer id);
    List<HashMap> findAll();
    Page<MiscellaneousCharge> findAll(Pageable pageable);
}
