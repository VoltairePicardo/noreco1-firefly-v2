package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.HouseWiringMaterialsIssuanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseWiringMaterialsIssuanceTicketRepo extends JpaRepository<HouseWiringMaterialsIssuanceTicket, Integer> {
    HouseWiringMaterialsIssuanceTicket findByDocId(Integer docId);
}
