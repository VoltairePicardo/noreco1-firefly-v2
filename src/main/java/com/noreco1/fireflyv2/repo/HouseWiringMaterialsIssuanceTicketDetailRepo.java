package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.HouseWiringMaterialsIssuanceTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseWiringMaterialsIssuanceTicketDetailRepo extends JpaRepository<HouseWiringMaterialsIssuanceTicketDetail, Integer> {
    List<HouseWiringMaterialsIssuanceTicketDetail> findAllByHouseWiringMaterialsIssuanceTicketId(Integer id);
}
