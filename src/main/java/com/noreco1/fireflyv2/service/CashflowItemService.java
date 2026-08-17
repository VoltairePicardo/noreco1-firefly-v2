package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.CashflowItemLog;

import java.util.Collection;
import java.util.List;

public interface CashflowItemService extends DataManagementService {
    List<CashflowItem> findAll();

    List<CashflowItem> findByIdNotIn();

    CashflowItem findById(Integer id);

    List<CashflowItemLog> getLogs(Integer id);
}
