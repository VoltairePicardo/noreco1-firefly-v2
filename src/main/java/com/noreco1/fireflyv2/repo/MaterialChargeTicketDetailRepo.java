package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialChargeTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface MaterialChargeTicketDetailRepo extends JpaRepository<MaterialChargeTicketDetail, Integer> {
    ArrayList<MaterialChargeTicketDetail> findByMaterialChargeTicketId(Integer id);
}
