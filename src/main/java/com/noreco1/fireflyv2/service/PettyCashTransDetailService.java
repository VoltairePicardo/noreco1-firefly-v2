package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.PettyCashTransDetail;

import java.util.HashMap;
import java.util.List;

public interface PettyCashTransDetailService {

    HashMap findById(Integer id);

    List<HashMap> findAll();

    List<PettyCashTransDetail> findByPCVId(Integer pcvId);
}
