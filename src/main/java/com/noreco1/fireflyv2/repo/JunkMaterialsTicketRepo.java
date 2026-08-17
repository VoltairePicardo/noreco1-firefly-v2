package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JunkMaterialsTicketRepo extends JpaRepository<JunkMaterialsTicket, Integer> {
    JunkMaterialsTicket findByDocId(Integer docId);
}
