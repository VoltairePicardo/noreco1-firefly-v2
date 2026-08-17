package com.noreco1.fireflyv2.service;

import java.util.HashMap;
import java.util.List;

public interface CashAdvanceParticularService {

    HashMap findById(Integer id);

    List<HashMap> findAll();

    List<HashMap> findByCAId(Integer pcvId);

    List<HashMap> findByCAIdForLiquidation(Integer id, Integer calId);
}
