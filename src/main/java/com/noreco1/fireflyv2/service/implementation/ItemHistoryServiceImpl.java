package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.repo.ItemTestingRepo;
import com.noreco1.fireflyv2.service.ItemHistoryService;
import com.noreco1.fireflyv2.mysql_repo.TurnOnOrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ItemHistoryServiceImpl implements ItemHistoryService {

    @Autowired
    ItemTestingRepo itemTestingRepo;

    @Autowired
    TurnOnOrderRepo turnOnOrderRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getItemHistory(String serialNumber) {

        List<Map> maps = new ArrayList<>();

        try {

            List<Object[]> mysqlData = this.itemTestingRepo.getItemHistory(serialNumber);

            if (Checker.collectionIsNotEmpty(mysqlData)){

                for (Object[] o : mysqlData){

                    maps.add(this.buildMap(o));

                }

            }

            List<Object[]> mssqlData = this.turnOnOrderRepo.getItemHistory(serialNumber);

            if (Checker.collectionIsNotEmpty(mssqlData)){

                for (Object[] o : mssqlData){

                    maps.add(this.buildMap(o));

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return maps;

    }

    private Map buildMap(Object[] o){

        Map rowMap = new HashMap();

        try {

            Date historyDate = (Date) o[0];
            String transactionType = (String) o[1];
            String referenceNumber = (String) o[2];
            String crew = (String) o[3];
            String remark = (String) o[4];
            String transactBy = (String) o[5];
            Date transactionDate = (Date) o[6];

            rowMap.put("historyDate", historyDate);
            rowMap.put("transactionType", transactionType);
            rowMap.put("referenceNumber", referenceNumber);
            rowMap.put("crew", crew);
            rowMap.put("remark", remark);
            rowMap.put("transactBy", transactBy);
            rowMap.put("transactionDate", transactionDate);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return rowMap;

    }

}
