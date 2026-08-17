package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CashflowItemType;

import java.util.List;

public interface CashflowItemTypeService extends DataManagementService {
    List<CashflowItemType> findAll();

    CashflowItemType findById(Integer id);
}
