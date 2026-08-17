package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.MaterialChargeTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MaterialChargeTicketService {
    Page<MaterialChargeTicket> findByDateRangeAndCode(String from, String to, String query, Pageable pageable);

    List<Map> getDetails(Integer id);
}
