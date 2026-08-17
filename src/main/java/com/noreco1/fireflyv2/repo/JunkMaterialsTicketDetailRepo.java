package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JunkMaterialsTicketDetailRepo extends JpaRepository<JunkMaterialsTicketDetail, Integer> {
    List<JunkMaterialsTicketDetail> findAllByJunkMaterialsTicketId(Integer id);
}
