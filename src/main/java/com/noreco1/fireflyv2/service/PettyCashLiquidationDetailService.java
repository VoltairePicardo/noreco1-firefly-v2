package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.PettyCashLiquidationDetail;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tonyc on 6/20/2023.
 */
public interface PettyCashLiquidationDetailService {

    HashMap findById(Integer id);

    List<HashMap> findAll();

    List<PettyCashLiquidationDetail> findByPCLId(Integer pcvId);
}
