package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.PettyCashFund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;

public interface PettyCashFundService extends DataManagementService {

    HashMap findById(Integer id);
    PettyCashFund findOne(Integer id);
    PettyCashFund findByOfficeId(Integer id);
    List<HashMap> findAll();

    Page<PettyCashFund> findAll(Pageable pageable);
    Page<PettyCashFund> find(String query, Pageable pageable);
}
