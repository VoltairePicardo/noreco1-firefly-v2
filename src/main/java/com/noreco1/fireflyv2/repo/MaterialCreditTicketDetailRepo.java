package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialCreditTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialCreditTicketDetailRepo extends JpaRepository<MaterialCreditTicketDetail, Integer> {
    List<MaterialCreditTicketDetail> findAllByMaterialCreditTicketId(Integer id);
}
