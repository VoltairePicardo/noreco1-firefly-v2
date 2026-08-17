package com.noreco1.fireflyv2.service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tonyc on 6/20/2023.
 */
public interface PettyCashLiquidationService extends VoucherService {

    HashMap findById(Integer id);

    List<HashMap> findAll();
}
