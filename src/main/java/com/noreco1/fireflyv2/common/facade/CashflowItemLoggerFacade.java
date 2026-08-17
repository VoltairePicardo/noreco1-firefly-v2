package com.noreco1.fireflyv2.common.facade;


import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.CashflowItemLog;

import java.util.List;

public interface CashflowItemLoggerFacade {
    void log(CashflowItem cashflowItem);
    List<CashflowItemLog> getLogs(Integer id);
}
