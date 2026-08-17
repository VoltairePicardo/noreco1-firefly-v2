package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.ItemTransactionDetailSerialNo;
import com.noreco1.fireflyv2.repo.ItemTransactionDetailSerialNoRepo;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemSerialNoFacadeImpl implements ItemSerialNoFacade {

    @Autowired
    ItemTransactionDetailSerialNoRepo itemTransactionDetailSerialNoRepo;

    @Override
    public JRBeanCollectionDataSource itemsInPrint(List<Map> details) {
        List<Map> itemLines = new ArrayList<>();

        try {

            if(!details.isEmpty()) {

                int counter = 1;
                for(Map detail: details) {
                    Integer stockTransactionDetailId = (Integer)detail.get("stockTransactionDetailId");

                    List<ItemTransactionDetailSerialNo> serialNos = itemTransactionDetailSerialNoRepo.findAllByItemTransactionDetailId(stockTransactionDetailId);

                    if(Checker.collectionIsNotEmpty(serialNos)) {

                        Map line = new HashMap();
                        line.put("code", detail.get("code"));
                        line.put("name", detail.get("description"));
                        line.put("quantity", detail.get("quantity"));
                        line.put("unit", detail.get("unitCode"));

                        for(ItemTransactionDetailSerialNo serialNo: serialNos) {
                            line.put("count", counter++);
                            line.put("serial", serialNo.getSerialNo());
                            itemLines.add(line);

                            line = new HashMap(); // reset line
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return  new JRBeanCollectionDataSource(itemLines);
    }
}
