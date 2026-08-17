package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StrategicInitiative;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategicInitiativeRepo extends JpaRepository<StrategicInitiative, Integer> {

    Page<StrategicInitiative> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    StrategicInitiative findByDepartmentIdAndDescriptionContainingIgnoreCase(Integer departmentId, String query);
    List<StrategicInitiative> findByOrderByDescriptionAsc();

    List<StrategicInitiative> findAllByDepartmentIdOrderByDescriptionAsc(Integer departmentId);

}
