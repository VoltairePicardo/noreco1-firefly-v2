package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SpecialEquipmentAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialEquipmentAssignmentLogRepo extends JpaRepository<SpecialEquipmentAssignmentLog, Integer> {
    List<SpecialEquipmentAssignmentLog> findBySpecialEquipmentAssignmentIdOrderByIdDesc(int id);
}
