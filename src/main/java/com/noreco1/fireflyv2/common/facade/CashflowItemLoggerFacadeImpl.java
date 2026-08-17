package com.noreco1.fireflyv2.common.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.CashflowItemLog;
import com.noreco1.fireflyv2.repo.CashflowItemLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class CashflowItemLoggerFacadeImpl implements CashflowItemLoggerFacade {

    @Autowired
    CashflowItemLogRepo cashflowItemLogRepo;

    @Override
    public void log(CashflowItem cashflowItem) {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            StringBuilder valueBuilder = new StringBuilder();

            Map valueMap = new HashMap<>();

            valueMap.put("id", cashflowItem.getId());
            valueMap.put("name", cashflowItem.getName());
            valueMap.put("ordinalNumber", cashflowItem.getOrdinalNumber());
            valueMap.put("type", cashflowItem.getCashflowItemType().getName());
            valueMap.put("parent", cashflowItem.getParentCashflowItem() == null ? "":cashflowItem.getParentCashflowItem().getName());
            valueMap.put("createdAt", cashflowItem.getCreatedAt());
            valueMap.put("updatedAt", cashflowItem.getUpdatedAt());
            valueMap.put("createdBy", cashflowItem.getCreatedBy() == null ? "":cashflowItem.getCreatedBy().getFullName());
            valueMap.put("modifiedBy", cashflowItem.getModifiedBy() == null ? "":cashflowItem.getModifiedBy().getFullName());
            valueMap.put("account", cashflowItem.getAccount() == null ? "":cashflowItem.getAccount().getCode() + " - " + cashflowItem.getAccount().getTitle());

            String valueStr = ow.writeValueAsString(valueMap);
            valueBuilder.append(valueStr);

            CashflowItemLog log = new CashflowItemLog();
            log.setLoggedBy(cashflowItem.getModifiedBy());
            log.setCashflowItem(cashflowItem);
            log.setCreatedAt(new Date());
            log.setValue(valueBuilder.toString());

            cashflowItemLogRepo.save(log);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<CashflowItemLog> getLogs(Integer id) {
        return cashflowItemLogRepo.findAllByCashflowItemIdOrderByCreatedAtDesc(id);
    }
}
