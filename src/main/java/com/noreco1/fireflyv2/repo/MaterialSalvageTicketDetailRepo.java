package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialSalvageTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialSalvageTicketDetailRepo extends JpaRepository<MaterialSalvageTicketDetail, Integer> {
    List<MaterialSalvageTicketDetail> findAllByMaterialSalvageTicketId(Integer id);
}
