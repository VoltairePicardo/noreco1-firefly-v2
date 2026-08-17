package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.MaterialChargeTicket;
import com.noreco1.fireflyv2.model.MaterialChargeTicketDetail;
import com.noreco1.fireflyv2.repo.MaterialChargeTicketDetailRepo;
import com.noreco1.fireflyv2.repo.MaterialChargeTicketRepo;
import com.noreco1.fireflyv2.service.MaterialChargeTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "materialChargeTicketServiceImpl")
public class MaterialChargeTicketServiceImpl implements MaterialChargeTicketService {

    @Autowired
    MaterialChargeTicketRepo materialChargeTicketRepo;

    @Autowired
    MaterialChargeTicketDetailRepo materialChargeTicketDetailRepo;

    @Override
    public Page<MaterialChargeTicket> findByDateRangeAndCode(String from, String to, String query, Pageable pageable) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        Page<MaterialChargeTicket> ret = null;
        if(query != null){
            ret = materialChargeTicketRepo.findAllByVoucherDateBetweenAndCodeOrderByVoucherDateAscCodeAsc(start, end, query, pageable);
        } else {
            ret = materialChargeTicketRepo.findAllByVoucherDateBetweenOrderByVoucherDateAscCodeAsc(start, end, pageable);
        }
        return ret;
    }

    @Override
    public List<Map> getDetails(Integer id) {
        List<Map> data = new ArrayList<>();

        MaterialChargeTicket materialChargeTicket = materialChargeTicketRepo.findById(id).orElse(null);
        if (materialChargeTicket != null) {

            List<MaterialChargeTicketDetail> details = materialChargeTicketDetailRepo.findByMaterialChargeTicketId(materialChargeTicket.getId());

            if(! details.isEmpty() ) {

                int counter = 1;
                for (MaterialChargeTicketDetail detail:details) {

                    Map detailMap = new HashMap();

                    detailMap.put("id", counter++);
                    detailMap.put("itemId", detail.getItem().getId());
                    detailMap.put("itemCode", detail.getItem().getCode());
                    detailMap.put("description", detail.getItem().getDescription());
                    detailMap.put("quantity", detail.getQuantity());
                    detailMap.put("unitId", detail.getItem().getUnit().getId());
                    detailMap.put("unitCode", detail.getItem().getUnit().getCode());
                    detailMap.put("quantity", detail.getQuantity());

                    data.add(detailMap);
                }
            }
        }

        return data;
    }
}
